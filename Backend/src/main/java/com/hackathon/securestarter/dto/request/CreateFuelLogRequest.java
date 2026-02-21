package com.hackathon.securestarter.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFuelLogRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    private UUID tripId; // optional

    @NotNull(message = "Liters is required")
    @Positive(message = "Liters must be positive")
    private Double liters;

    @NotNull(message = "Cost is required")
    @PositiveOrZero(message = "Cost must be zero or positive")
    private BigDecimal cost;

    @PositiveOrZero(message = "Odometer reading must be zero or positive")
    private Double odometerAtFill;

    @NotNull(message = "Fill date is required")
    private LocalDateTime fillDate;
}
