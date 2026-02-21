package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.request.CreateMaintenanceLogRequest;
import com.hackathon.securestarter.dto.request.UpdateMaintenanceLogRequest;
import com.hackathon.securestarter.dto.request.UpdateMaintenanceStatusRequest;
import com.hackathon.securestarter.dto.response.MaintenanceLogResponse;
import com.hackathon.securestarter.dto.response.MessageResponse;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.enums.MaintenanceStatus;
import com.hackathon.securestarter.service.MaintenanceLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@Slf4j
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    // ===== WRITE Operations (FLEET_MANAGER only) =====

    @PostMapping
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<MaintenanceLogResponse> createMaintenanceLog(
            @Valid @RequestBody CreateMaintenanceLogRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Create maintenance log by user: {}", currentUser.getEmail());
        MaintenanceLogResponse response = maintenanceLogService.createMaintenanceLog(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<MaintenanceLogResponse> updateMaintenanceLog(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaintenanceLogRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Update maintenance log {} by user: {}", id, currentUser.getEmail());
        MaintenanceLogResponse response = maintenanceLogService.updateMaintenanceLog(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<MaintenanceLogResponse> updateMaintenanceStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaintenanceStatusRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Update maintenance log {} status by user: {}", id, currentUser.getEmail());
        MaintenanceLogResponse response = maintenanceLogService.updateMaintenanceStatus(
                id, request.getStatus(), currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<MessageResponse> deleteMaintenanceLog(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Delete maintenance log {} by user: {}", id, currentUser.getEmail());
        maintenanceLogService.deleteMaintenanceLog(id, currentUser);
        return ResponseEntity.ok(MessageResponse.success("Maintenance log deleted successfully"));
    }

    // ===== READ Operations (FLEET_MANAGER, SAFETY_OFFICER, FINANCIAL_ANALYST) =====

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST')")
    public ResponseEntity<MaintenanceLogResponse> getMaintenanceLogById(@PathVariable UUID id) {
        MaintenanceLogResponse response = maintenanceLogService.getMaintenanceLogById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST')")
    public ResponseEntity<List<MaintenanceLogResponse>> getAllMaintenanceLogs(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) MaintenanceStatus status) {
        List<MaintenanceLogResponse> responses;
        if (vehicleId != null) {
            responses = maintenanceLogService.getMaintenanceLogsByVehicle(vehicleId);
        } else if (status != null) {
            responses = maintenanceLogService.getMaintenanceLogsByStatus(status);
        } else {
            responses = maintenanceLogService.getAllMaintenanceLogs();
        }
        return ResponseEntity.ok(responses);
    }
}
