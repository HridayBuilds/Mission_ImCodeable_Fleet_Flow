package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.response.DashboardResponse;
import com.hackathon.securestarter.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Dashboard is accessible to all authenticated roles.
     * Returns KPIs and recent trip data.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'DISPATCHER', 'SAFETY_OFFICER', 'FINANCIAL_ANALYST')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.info("Dashboard data requested");
        DashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(response);
    }
}
