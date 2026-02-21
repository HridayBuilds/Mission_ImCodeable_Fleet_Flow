package com.hackathon.securestarter.dto.request;

import com.hackathon.securestarter.enums.VehicleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleRequest {

    @NotBlank(message = "License plate is required")
    @Size(max = 30, message = "License plate cannot exceed 30 characters")
    private String licensePlate;

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Model cannot exceed 100 characters")
    private String model;

    @NotNull(message = "Vehicle type is required")
    private VehicleType type;

    @NotNull(message = "Max load capacity is required")
    @Positive(message = "Max load capacity must be positive")
    private Double maxLoadCapacity;

    @PositiveOrZero(message = "Initial odometer must be zero or positive")
    private Double odometer;

    @PositiveOrZero(message = "Acquisition cost must be zero or positive")
    private BigDecimal acquisitionCost;
}
