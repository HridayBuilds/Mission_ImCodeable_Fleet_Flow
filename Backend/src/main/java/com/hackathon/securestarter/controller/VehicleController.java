package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.request.CreateVehicleRequest;
import com.hackathon.securestarter.dto.request.UpdateVehicleRequest;
import com.hackathon.securestarter.dto.response.MessageResponse;
import com.hackathon.securestarter.dto.response.VehicleResponse;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.enums.VehicleStatus;
import com.hackathon.securestarter.enums.VehicleType;
import com.hackathon.securestarter.service.VehicleService;
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
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;

    // ===== WRITE Operations (FLEET_MANAGER only) =====

    @PostMapping
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Create vehicle request by user: {}", currentUser.getEmail());
        VehicleResponse response = vehicleService.createVehicle(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Update vehicle {} by user: {}", id, currentUser.getEmail());
        VehicleResponse response = vehicleService.updateVehicle(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<VehicleResponse> updateVehicleStatus(
            @PathVariable UUID id,
            @RequestParam VehicleStatus status,
            @AuthenticationPrincipal User currentUser) {
        log.info("Update vehicle {} status to {} by user: {}", id, status, currentUser.getEmail());
        VehicleResponse response = vehicleService.updateVehicleStatus(id, status, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<MessageResponse> deleteVehicle(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Delete vehicle {} by user: {}", id, currentUser.getEmail());
        vehicleService.deleteVehicle(id, currentUser);
        return ResponseEntity.ok(MessageResponse.success("Vehicle deleted successfully"));
    }

    // ===== READ Operations (FLEET_MANAGER, DISPATCHER, SAFETY_OFFICER) =====

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST')")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable UUID id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST')")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) VehicleType type) {
        List<VehicleResponse> responses;
        if (status != null) {
            responses = vehicleService.getVehiclesByStatus(status);
        } else if (type != null) {
            responses = vehicleService.getVehiclesByType(type);
        } else {
            responses = vehicleService.getAllVehicles();
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'DISPATCHER')")
    public ResponseEntity<List<VehicleResponse>> getAvailableVehicles() {
        List<VehicleResponse> responses = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(responses);
    }
}
