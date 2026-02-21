package com.hackathon.securestarter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyFinancialSummaryResponse {

    private Integer year;
    private Integer month;
    private BigDecimal revenue;
    private BigDecimal fuelCost;
    private BigDecimal maintenanceCost;
    private BigDecimal netProfit;
    private Integer totalTrips;
    private Double totalDistance;
    private Double totalFuelLiters;
}
