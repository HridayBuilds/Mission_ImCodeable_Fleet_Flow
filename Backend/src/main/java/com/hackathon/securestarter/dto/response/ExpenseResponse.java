package com.hackathon.securestarter.dto.response;

import com.hackathon.securestarter.enums.ExpenseStatus;
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
public class ExpenseResponse {

    private UUID id;
    private UUID tripId;
    private Long tripNumber;
    private UUID vehicleId;
    private String vehicleName;
    private String vehicleLicensePlate;
    private UUID driverId;
    private String driverName;
    private Double distance;
    private BigDecimal fuelCost;
    private BigDecimal miscExpense;
    private BigDecimal totalCost;
    private ExpenseStatus status;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
