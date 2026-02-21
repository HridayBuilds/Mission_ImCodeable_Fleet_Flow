package com.hackathon.securestarter.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {

    @NotNull(message = "Trip ID is required")
    private UUID tripId;

    @PositiveOrZero(message = "Distance must be zero or positive")
    private Double distance;

    @NotNull(message = "Fuel cost is required")
    @PositiveOrZero(message = "Fuel cost must be zero or positive")
    private BigDecimal fuelCost;

    @PositiveOrZero(message = "Misc expense must be zero or positive")
    private BigDecimal miscExpense;
}
