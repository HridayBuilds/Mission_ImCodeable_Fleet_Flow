package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.request.CreateDriverRequest;
import com.hackathon.securestarter.dto.request.UpdateDriverRequest;
import com.hackathon.securestarter.dto.response.DriverResponse;
import com.hackathon.securestarter.dto.response.MessageResponse;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.enums.DriverStatus;
import com.hackathon.securestarter.service.DriverService;
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
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final DriverService driverService;

    // ===== WRITE Operations (SAFETY_OFFICER only) =====

    @PostMapping
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody CreateDriverRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Create driver request by user: {}", currentUser.getEmail());
        DriverResponse response = driverService.createDriver(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDriverRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Update driver {} by user: {}", id, currentUser.getEmail());
        DriverResponse response = driverService.updateDriver(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    public ResponseEntity<DriverResponse> updateDriverStatus(
            @PathVariable UUID id,
            @RequestParam DriverStatus status,
            @AuthenticationPrincipal User currentUser) {
        log.info("Update driver {} status to {} by user: {}", id, status, currentUser.getEmail());
        DriverResponse response = driverService.updateDriverStatus(id, status, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complaint")
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    public ResponseEntity<DriverResponse> addComplaint(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Add complaint to driver {} by user: {}", id, currentUser.getEmail());
        DriverResponse response = driverService.addComplaint(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    public ResponseEntity<MessageResponse> deleteDriver(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Delete driver {} by user: {}", id, currentUser.getEmail());
        driverService.deleteDriver(id, currentUser);
        return ResponseEntity.ok(MessageResponse.success("Driver deleted successfully"));
    }

    // ===== READ Operations (SAFETY_OFFICER, FLEET_MANAGER, DISPATCHER) =====

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER', 'FLEET_MANAGER', 'DISPATCHER')")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable UUID id) {
        DriverResponse response = driverService.getDriverById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER', 'FLEET_MANAGER', 'DISPATCHER')")
    public ResponseEntity<List<DriverResponse>> getAllDrivers(
            @RequestParam(required = false) DriverStatus status) {
        List<DriverResponse> responses;
        if (status != null) {
            responses = driverService.getDriversByStatus(status);
        } else {
            responses = driverService.getAllDrivers();
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER', 'FLEET_MANAGER', 'DISPATCHER')")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        List<DriverResponse> responses = driverService.getAvailableDrivers();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/expired-license")
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    public ResponseEntity<List<DriverResponse>> getDriversWithExpiredLicense() {
        List<DriverResponse> responses = driverService.getDriversWithExpiredLicense();
        return ResponseEntity.ok(responses);
    }
}
