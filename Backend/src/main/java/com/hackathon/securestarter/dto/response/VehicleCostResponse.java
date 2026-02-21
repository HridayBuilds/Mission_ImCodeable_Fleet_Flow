package com.hackathon.securestarter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCostResponse {

    private UUID vehicleId;
    private String vehicleName;
    private String vehicleLicensePlate;
    private BigDecimal totalExpenseCost;
    private BigDecimal totalFuelCost;
    private BigDecimal totalMaintenanceCost;
    private BigDecimal totalOperationalCost; // expense + maintenance
    private Double totalDistance;
    private Double fuelEfficiency;           // km per liter
    private BigDecimal costPerKm;
}
