package com.hackathon.securestarter.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTripRequest {

    @NotNull(message = "End odometer reading is required")
    @Positive(message = "End odometer must be positive")
    private Double endOdometer;

    @PositiveOrZero(message = "Revenue must be zero or positive")
    private BigDecimal revenue;
}
