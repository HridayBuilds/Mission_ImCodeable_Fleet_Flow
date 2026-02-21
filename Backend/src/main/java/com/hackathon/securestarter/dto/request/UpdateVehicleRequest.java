package com.hackathon.securestarter.dto.request;

import com.hackathon.securestarter.enums.VehicleType;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleRequest {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Model cannot exceed 100 characters")
    private String model;

    private VehicleType type;

    @PositiveOrZero(message = "Max load capacity must be zero or positive")
    private Double maxLoadCapacity;

    @PositiveOrZero(message = "Acquisition cost must be zero or positive")
    private BigDecimal acquisitionCost;
}
