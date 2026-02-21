package com.hackathon.securestarter.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotNull(message = "Cargo weight is required")
    @Positive(message = "Cargo weight must be positive")
    private Double cargoWeight;

    @NotBlank(message = "Origin is required")
    @Size(max = 255, message = "Origin cannot exceed 255 characters")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(max = 255, message = "Destination cannot exceed 255 characters")
    private String destination;

    @PositiveOrZero(message = "Estimated fuel cost must be zero or positive")
    private BigDecimal estimatedFuelCost;
}
