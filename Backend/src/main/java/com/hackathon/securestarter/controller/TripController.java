package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.request.CancelTripRequest;
import com.hackathon.securestarter.dto.request.CompleteTripRequest;
import com.hackathon.securestarter.dto.request.CreateTripRequest;
import com.hackathon.securestarter.dto.response.TripResponse;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.enums.TripStatus;
import com.hackathon.securestarter.service.TripService;
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
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;

    // ===== WRITE Operations (DISPATCHER only) =====

    @PostMapping
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody CreateTripRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Create trip request by user: {}", currentUser.getEmail());
        TripResponse response = tripService.createTrip(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/dispatch")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> dispatchTrip(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Dispatch trip {} by user: {}", id, currentUser.getEmail());
        TripResponse response = tripService.dispatchTrip(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/in-transit")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> markInTransit(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Mark trip {} in-transit by user: {}", id, currentUser.getEmail());
        TripResponse response = tripService.markInTransit(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> completeTrip(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteTripRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Complete trip {} by user: {}", id, currentUser.getEmail());
        TripResponse response = tripService.completeTrip(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<TripResponse> cancelTrip(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelTripRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Cancel trip {} by user: {}", id, currentUser.getEmail());
        TripResponse response = tripService.cancelTrip(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    // ===== READ Operations (DISPATCHER and FLEET_MANAGER) =====

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'FINANCIAL_ANALYST')")
    public ResponseEntity<TripResponse> getTripById(@PathVariable UUID id) {
        TripResponse response = tripService.getTripById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'FLEET_MANAGER', 'FINANCIAL_ANALYST')")
    public ResponseEntity<List<TripResponse>> getAllTrips(
            @RequestParam(required = false) TripStatus status,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId) {
        List<TripResponse> responses;
        if (status != null) {
            responses = tripService.getTripsByStatus(status);
        } else if (vehicleId != null) {
            responses = tripService.getTripsByVehicle(vehicleId);
        } else if (driverId != null) {
            responses = tripService.getTripsByDriver(driverId);
        } else {
            responses = tripService.getAllTrips();
        }
        return ResponseEntity.ok(responses);
    }
}
