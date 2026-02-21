package com.hackathon.securestarter.dto.response;

import com.hackathon.securestarter.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {

    private UUID id;
    private String name;
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private String licenseCategory;
    private String phone;
    private Double completionRate;
    private Double safetyScore;
    private Integer complaints;
    private Integer totalTripsAssigned;
    private Integer totalTripsCompleted;
    private DriverStatus status;
    private Boolean licenseExpired;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
