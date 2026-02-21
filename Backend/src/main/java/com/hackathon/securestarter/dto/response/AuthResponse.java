package com.hackathon.securestarter.dto.response;

import com.hackathon.securestarter.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UUID userId;
    private String email;
    private String name;
    private String employeeId;
    private Role role;
    private Boolean isVerified;

    public AuthResponse(String accessToken, UUID userId, String email, String name,
                        String employeeId, Role role, Boolean isVerified) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.employeeId = employeeId;
        this.role = role;
        this.isVerified = isVerified;
    }
}