package com.hackathon.securestarter.dto.response;

import com.hackathon.securestarter.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String employeeId;
    private String phone;
    private Role role;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
}