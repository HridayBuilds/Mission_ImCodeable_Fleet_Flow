package com.hackathon.securestarter.dto.request;

import com.hackathon.securestarter.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMaintenanceStatusRequest {

    @NotNull(message = "Status is required")
    private MaintenanceStatus status;
}
