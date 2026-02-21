package com.hackathon.securestarter.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaintenanceLogRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    @Size(max = 200, message = "Service name cannot exceed 200 characters")
    private String serviceName;

    @Size(max = 1000, message = "Issue description cannot exceed 1000 characters")
    private String issueDescription;

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    @PositiveOrZero(message = "Cost must be zero or positive")
    private BigDecimal cost;
}
