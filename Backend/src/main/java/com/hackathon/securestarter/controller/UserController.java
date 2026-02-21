package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.request.ChangePasswordRequest;
import com.hackathon.securestarter.dto.request.UpdateProfileRequest;
import com.hackathon.securestarter.dto.response.MessageResponse;
import com.hackathon.securestarter.dto.response.UserResponse;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     * GET /api/users/me
     * Requires: JWT token in Authorization header
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        log.info("Get profile request for user: {}", currentUser.getEmail());
        UserResponse response = userService.getCurrentUserProfile(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user profile
     * PUT /api/users/update-profile
     * Requires: JWT token in Authorization header
     */
    @PutMapping("/update-profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("Update profile request for user: {}", currentUser.getEmail());
        UserResponse response = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Change password for current user
     * POST /api/users/change-password
     * Requires: JWT token in Authorization header
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Change password request for user: {}", currentUser.getEmail());
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

}