package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.response.MonthlyFinancialSummaryResponse;
import com.hackathon.securestarter.dto.response.VehicleCostResponse;
import com.hackathon.securestarter.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ===== READ Operations (FINANCIAL_ANALYST full, FLEET_MANAGER view) =====

    @GetMapping("/financial-summaries")
    @PreAuthorize("hasAnyRole('FINANCIAL_ANALYST', 'FLEET_MANAGER')")
    public ResponseEntity<List<MonthlyFinancialSummaryResponse>> getFinancialSummaries(
            @RequestParam(required = false) Integer year) {
        log.info("Financial summaries requested for year: {}", year);
        List<MonthlyFinancialSummaryResponse> responses = analyticsService.getFinancialSummaries(year);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/vehicle-costs")
    @PreAuthorize("hasAnyRole('FINANCIAL_ANALYST', 'FLEET_MANAGER')")
    public ResponseEntity<List<VehicleCostResponse>> getVehicleCosts() {
        log.info("Vehicle cost analytics requested");
        List<VehicleCostResponse> responses = analyticsService.getVehicleCosts();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/top-costliest-vehicles")
    @PreAuthorize("hasAnyRole('FINANCIAL_ANALYST', 'FLEET_MANAGER')")
    public ResponseEntity<List<VehicleCostResponse>> getTopCostliestVehicles(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Top {} costliest vehicles requested", limit);
        List<VehicleCostResponse> responses = analyticsService.getTopCostliestVehicles(limit);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fleet-summary")
    @PreAuthorize("hasAnyRole('FINANCIAL_ANALYST', 'FLEET_MANAGER')")
    public ResponseEntity<AnalyticsService.FleetAnalyticsSummary> getFleetSummary() {
        log.info("Fleet analytics summary requested");
        AnalyticsService.FleetAnalyticsSummary response = analyticsService.getFleetAnalyticsSummary();
        return ResponseEntity.ok(response);
    }

    // ===== WRITE Operations (FINANCIAL_ANALYST only — generate summaries) =====

    @PostMapping("/generate-summary")
    @PreAuthorize("hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<MonthlyFinancialSummaryResponse> generateMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {
        log.info("Generate monthly summary for {}/{}", year, month);
        MonthlyFinancialSummaryResponse response = analyticsService.generateMonthlySummary(year, month);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-current-summary")
    @PreAuthorize("hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<MonthlyFinancialSummaryResponse> generateCurrentMonthSummary() {
        log.info("Generate current month summary");
        MonthlyFinancialSummaryResponse response = analyticsService.generateCurrentMonthSummary();
        return ResponseEntity.ok(response);
    }
}
