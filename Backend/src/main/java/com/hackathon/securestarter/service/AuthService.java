package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.request.ForgotPasswordRequest;
import com.hackathon.securestarter.dto.request.LoginRequest;
import com.hackathon.securestarter.dto.request.PasswordResetRequest;
import com.hackathon.securestarter.dto.request.SignupRequest;
import com.hackathon.securestarter.dto.response.AuthResponse;
import com.hackathon.securestarter.dto.response.MessageResponse;
import com.hackathon.securestarter.entity.PasswordResetToken;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.entity.VerificationToken;
import com.hackathon.securestarter.exception.BadRequestException;
import com.hackathon.securestarter.exception.ResourceNotFoundException;
import com.hackathon.securestarter.repository.PasswordResetTokenRepository;
import com.hackathon.securestarter.repository.UserRepository;
import com.hackathon.securestarter.repository.VerificationTokenRepository;
import com.hackathon.securestarter.security.JwtService;
import com.hackathon.securestarter.util.Constants;
import com.hackathon.securestarter.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;

    /**
     * Register new user
     */
    @Transactional
    public MessageResponse signup(SignupRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(Constants.EMAIL_ALREADY_EXISTS);
        }

        // Check if employee ID already exists
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException(Constants.EMPLOYEE_ID_ALREADY_EXISTS);
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .employeeId(request.getEmployeeId())
                .phone(request.getPhone())
                .role(request.getRole())
                .isVerified(false)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // Create verification token
        String token = tokenGenerator.generateVerificationToken();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusHours(Constants.VERIFICATION_TOKEN_EXPIRATION_HOURS))
                .build();

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        return MessageResponse.success(Constants.SIGNUP_SUCCESS);
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
        // Find user first
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_CREDENTIALS));

        // Proceed with authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Check if account is verified
        if (!user.getIsVerified()) {
            throw new BadRequestException(Constants.ACCOUNT_NOT_VERIFIED);
        }

        // Check if account is active
        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated. Please contact an administrator.");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user);

        log.info("User logged in: {}", user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getEmployeeId(),
                user.getRole(),
                user.getIsVerified()
        );
    }

    /**
     * Verify email with token
     */
    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_TOKEN));

        // Check if token is expired
        if (verificationToken.isExpired()) {
            throw new BadRequestException(Constants.TOKEN_EXPIRED);
        }

        // Check if token is already used
        if (verificationToken.getIsUsed()) {
            throw new BadRequestException(Constants.TOKEN_ALREADY_USED);
        }

        // Verify user
        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        // Mark token as used
        verificationToken.setIsUsed(true);
        verificationTokenRepository.save(verificationToken);

        log.info("Email verified for user: {}", user.getEmail());

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        return MessageResponse.success(Constants.VERIFICATION_SUCCESS);
    }

    /**
     * Request password reset
     */
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

        // Generate reset token
        String token = tokenGenerator.generatePasswordResetToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(Constants.PASSWORD_RESET_TOKEN_EXPIRATION_HOURS))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Password reset requested for user: {}", user.getEmail());

        return MessageResponse.success(Constants.PASSWORD_RESET_EMAIL_SENT);
    }

    /**
     * Reset password with token
     */
    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_TOKEN));

        // Check if token is expired
        if (resetToken.isExpired()) {
            throw new BadRequestException(Constants.TOKEN_EXPIRED);
        }

        // Check if token is already used
        if (resetToken.getIsUsed()) {
            throw new BadRequestException(Constants.TOKEN_ALREADY_USED);
        }

        // Update password
        User user = resetToken.getUser();

        // Old password cannot be reused
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException(Constants.PASSWORD_CANNOT_BE_SAME_AS_OLD);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());

        return MessageResponse.success(Constants.PASSWORD_RESET_SUCCESS);
    }

    /**
     * Validate password reset token (check if valid, not expired, not used)
     */
    public MessageResponse validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException(Constants.INVALID_TOKEN));

        // Check if token is expired
        if (resetToken.isExpired()) {
            throw new BadRequestException(Constants.TOKEN_EXPIRED);
        }

        // Check if token is already used
        if (resetToken.getIsUsed()) {
            throw new BadRequestException(Constants.TOKEN_ALREADY_USED);
        }

        log.info("Password reset token validated successfully");

        return MessageResponse.success("Token is valid. You can now reset your password.");
    }
}