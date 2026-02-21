package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.request.CreateFuelLogRequest;
import com.hackathon.securestarter.dto.response.FuelLogResponse;
import com.hackathon.securestarter.dto.response.MessageResponse;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.service.FuelLogService;
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
@RequestMapping("/api/fuel-logs")
@RequiredArgsConstructor
@Slf4j
public class FuelLogController {

    private final FuelLogService fuelLogService;

    // ===== WRITE Operations (FINANCIAL_ANALYST only) =====

    @PostMapping
    @PreAuthorize("hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<FuelLogResponse> createFuelLog(
            @Valid @RequestBody CreateFuelLogRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Create fuel log by user: {}", currentUser.getEmail());
        FuelLogResponse response = fuelLogService.createFuelLog(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<MessageResponse> deleteFuelLog(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Delete fuel log {} by user: {}", id, currentUser.getEmail());
        fuelLogService.deleteFuelLog(id, currentUser);
        return ResponseEntity.ok(MessageResponse.success("Fuel log deleted successfully"));
    }

    // ===== READ Operations (FINANCIAL_ANALYST and FLEET_MANAGER) =====

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FINANCIAL_ANALYST', 'FLEET_MANAGER')")
    public ResponseEntity<FuelLogResponse> getFuelLogById(@PathVariable UUID id) {
        FuelLogResponse response = fuelLogService.getFuelLogById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCIAL_ANALYST', 'FLEET_MANAGER')")
    public ResponseEntity<List<FuelLogResponse>> getAllFuelLogs(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID tripId) {
        List<FuelLogResponse> responses;
        if (vehicleId != null) {
            responses = fuelLogService.getFuelLogsByVehicle(vehicleId);
        } else if (tripId != null) {
            responses = fuelLogService.getFuelLogsByTrip(tripId);
        } else {
            responses = fuelLogService.getAllFuelLogs();
        }
        return ResponseEntity.ok(responses);
    }
}
