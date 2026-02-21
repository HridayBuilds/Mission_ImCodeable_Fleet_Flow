package com.hackathon.securestarter.dto.response;

import com.hackathon.securestarter.enums.VehicleStatus;
import com.hackathon.securestarter.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private UUID id;
    private String licensePlate;
    private String name;
    private String model;
    private VehicleType type;
    private Double maxLoadCapacity;
    private Double odometer;
    private VehicleStatus status;
    private BigDecimal acquisitionCost;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
