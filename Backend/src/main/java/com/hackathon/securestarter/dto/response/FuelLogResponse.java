package com.hackathon.securestarter.dto.response;

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
public class FuelLogResponse {

    private UUID id;
    private UUID vehicleId;
    private String vehicleName;
    private String vehicleLicensePlate;
    private UUID tripId;
    private Long tripNumber;
    private Double liters;
    private BigDecimal cost;
    private Double odometerAtFill;
    private LocalDateTime fillDate;
    private String recordedByName;
    private LocalDateTime createdAt;
}
