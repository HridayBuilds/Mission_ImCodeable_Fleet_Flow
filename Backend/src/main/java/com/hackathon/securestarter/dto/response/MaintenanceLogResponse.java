package com.hackathon.securestarter.dto.response;

import com.hackathon.securestarter.enums.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLogResponse {

    private UUID id;
    private UUID vehicleId;
    private String vehicleName;
    private String vehicleLicensePlate;
    private String serviceName;
    private String issueDescription;
    private LocalDate serviceDate;
    private BigDecimal cost;
    private MaintenanceStatus status;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
