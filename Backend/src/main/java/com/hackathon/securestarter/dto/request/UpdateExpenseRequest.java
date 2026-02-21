package com.hackathon.securestarter.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpenseRequest {

    @PositiveOrZero(message = "Distance must be zero or positive")
    private Double distance;

    @PositiveOrZero(message = "Fuel cost must be zero or positive")
    private BigDecimal fuelCost;

    @PositiveOrZero(message = "Misc expense must be zero or positive")
    private BigDecimal miscExpense;
}
