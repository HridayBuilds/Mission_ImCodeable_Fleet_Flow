package com.hackathon.securestarter.dto.response;

import com.hackathon.securestarter.enums.TripStatus;
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
public class TripResponse {

    private UUID id;
    private Long tripNumber;
    private UUID vehicleId;
    private String vehicleName;
    private String vehicleLicensePlate;
    private UUID driverId;
    private String driverName;
    private Double cargoWeight;
    private String origin;
    private String destination;
    private BigDecimal estimatedFuelCost;
    private Double actualDistance;
    private Double startOdometer;
    private Double endOdometer;
    private BigDecimal revenue;
    private TripStatus status;
    private String dispatchedByName;
    private LocalDateTime dispatchedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
