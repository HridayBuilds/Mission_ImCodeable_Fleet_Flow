package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.request.ForgotPasswordRequest;
import com.hackathon.securestarter.dto.request.LoginRequest;
import com.hackathon.securestarter.dto.request.PasswordResetRequest;
import com.hackathon.securestarter.dto.request.SignupRequest;
import com.hackathon.securestarter.dto.response.AuthResponse;
import com.hackathon.securestarter.dto.response.MessageResponse;
import com.hackathon.securestarter.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for email: {}", request.getEmail());
        MessageResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify email with token
     * GET /api/auth/verify?token=xyz
     */
    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam("token") String token) {
        log.info("Email verification request received");
        MessageResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request received for email: {}", request.getEmail());
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reset password with token
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset request received");
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint (public)
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse("Auth service is running"));
    }

    /**
     * Validate password reset token (Optional - for better UX)
     * GET /api/auth/validate-reset-token?token=xyz
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<MessageResponse> validateResetToken(@RequestParam("token") String token) {
        log.info("Password reset token validation request received");
        MessageResponse response = authService.validateResetToken(token);
        return ResponseEntity.ok(response);
    }

}

/*
EMAIL TOKEN VERIFICATION:
Email: http://localhost:8080/api/auth/verify?token=xyz works on POSTMAN, as testing directly for backend
Above is for email verification (to do in postman) but email will receive the link to the frontend, and frontend will send the request to the above link
received link on mail is: http://localhost:5173/verify?token=36d8864c-fb95...

PASSWORD RESET TOKEN VERIFICATION
Email: http://localhost:8080/api/auth/reset-password
Above is for email i received for password reset, but email will receive link to the frontend, and frontend will send the request to the above link
received link on mail is: http://localhost:5173/reset-password?token=e2241663-e987-4469-9938-e6b707cf45cc

 */