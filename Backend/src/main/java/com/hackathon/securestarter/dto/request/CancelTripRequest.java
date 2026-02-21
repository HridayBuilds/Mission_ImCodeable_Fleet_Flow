package com.hackathon.securestarter.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelTripRequest {

    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String cancellationReason;
}
