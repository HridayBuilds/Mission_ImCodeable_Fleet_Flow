package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.response.MonthlyFinancialSummaryResponse;
import com.hackathon.securestarter.dto.response.VehicleCostResponse;
import com.hackathon.securestarter.entity.MonthlyFinancialSummary;
import com.hackathon.securestarter.entity.Vehicle;
import com.hackathon.securestarter.enums.VehicleStatus;
import com.hackathon.securestarter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final VehicleRepository vehicleRepository;
    private final ExpenseRepository expenseRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final FuelLogRepository fuelLogRepository;
    private final TripRepository tripRepository;
    private final MonthlyFinancialSummaryRepository financialSummaryRepository;

    /**
     * Get monthly financial summaries for a specific year, or all summaries if year is null.
     */
    public List<MonthlyFinancialSummaryResponse> getFinancialSummaries(Integer year) {
        List<MonthlyFinancialSummary> summaries;
        if (year != null) {
            summaries = financialSummaryRepository.findByYearOrderByMonthAsc(year);
        } else {
            summaries = financialSummaryRepository.findAllByOrderByYearDescMonthDesc();
        }
        return summaries.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Generate/update the monthly financial summary for a given month.
     * Aggregates from trips, expenses, maintenance logs, and fuel logs.
     */
    @Transactional
    public MonthlyFinancialSummaryResponse generateMonthlySummary(int year, int month) {
        MonthlyFinancialSummary summary = financialSummaryRepository
                .findByYearAndMonth(year, month)
                .orElse(MonthlyFinancialSummary.builder()
                        .year(year)
                        .month(month)
                        .build());

        // Calculate revenue from completed trips in this month
        BigDecimal revenue = tripRepository.findByStatus(com.hackathon.securestarter.enums.TripStatus.COMPLETED)
                .stream()
                .filter(t -> t.getCompletedAt() != null
                        && t.getCompletedAt().getYear() == year
                        && t.getCompletedAt().getMonthValue() == month)
                .map(t -> t.getRevenue() != null ? t.getRevenue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total fuel cost from expenses
        BigDecimal fuelCost = expenseRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null
                        && e.getCreatedAt().getYear() == year
                        && e.getCreatedAt().getMonthValue() == month)
                .map(e -> e.getFuelCost() != null ? e.getFuelCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate maintenance cost
        BigDecimal maintenanceCost = maintenanceLogRepository.totalCostByMonth(year, month);

        // Trip count and distance
        long totalTrips = tripRepository.findByStatus(com.hackathon.securestarter.enums.TripStatus.COMPLETED)
                .stream()
                .filter(t -> t.getCompletedAt() != null
                        && t.getCompletedAt().getYear() == year
                        && t.getCompletedAt().getMonthValue() == month)
                .count();

        double totalDistance = tripRepository.findByStatus(com.hackathon.securestarter.enums.TripStatus.COMPLETED)
                .stream()
                .filter(t -> t.getCompletedAt() != null
                        && t.getCompletedAt().getYear() == year
                        && t.getCompletedAt().getMonthValue() == month)
                .mapToDouble(t -> t.getActualDistance() != null ? t.getActualDistance() : 0.0)
                .sum();

        // Total fuel liters
        double totalFuelLiters = fuelLogRepository.findAll().stream()
                .filter(f -> f.getFillDate() != null
                        && f.getFillDate().getYear() == year
                        && f.getFillDate().getMonthValue() == month)
                .mapToDouble(f -> f.getLiters() != null ? f.getLiters() : 0.0)
                .sum();

        summary.setRevenue(revenue);
        summary.setFuelCost(fuelCost);
        summary.setMaintenanceCost(maintenanceCost);
        summary.setTotalTrips((int) totalTrips);
        summary.setTotalDistance(totalDistance);
        summary.setTotalFuelLiters(totalFuelLiters);

        MonthlyFinancialSummary saved = financialSummaryRepository.save(summary);
        log.info("Monthly financial summary generated for {}/{}", year, month);
        return mapToSummaryResponse(saved);
    }

    /**
     * Get cost breakdown per vehicle for analytics.
     */
    public List<VehicleCostResponse> getVehicleCosts() {
        List<Vehicle> vehicles = vehicleRepository.findAll().stream()
                .filter(v -> v.getStatus() != VehicleStatus.RETIRED)
                .collect(Collectors.toList());

        List<VehicleCostResponse> responses = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            UUID vehicleId = vehicle.getId();

            BigDecimal totalExpenseCost = expenseRepository.totalCostByVehicleId(vehicleId);
            BigDecimal totalFuelCost = expenseRepository.totalFuelCostByVehicleId(vehicleId);
            BigDecimal totalMaintenanceCost = maintenanceLogRepository.totalCostByVehicleId(vehicleId);
            BigDecimal totalOperationalCost = totalExpenseCost.add(totalMaintenanceCost);

            // Calculate total distance from fuel logs or expenses
            Double totalLiters = fuelLogRepository.totalLitersByVehicleId(vehicleId);
            double totalDistance = vehicle.getOdometer() != null ? vehicle.getOdometer() : 0.0;

            // Fuel efficiency: km per liter
            double fuelEfficiency = 0.0;
            if (totalLiters != null && totalLiters > 0 && totalDistance > 0) {
                fuelEfficiency = totalDistance / totalLiters;
                fuelEfficiency = Math.round(fuelEfficiency * 100.0) / 100.0;
            }

            // Cost per km
            BigDecimal costPerKm = BigDecimal.ZERO;
            if (totalDistance > 0) {
                costPerKm = totalOperationalCost.divide(
                        BigDecimal.valueOf(totalDistance), 2, RoundingMode.HALF_UP);
            }

            responses.add(VehicleCostResponse.builder()
                    .vehicleId(vehicleId)
                    .vehicleName(vehicle.getName())
                    .vehicleLicensePlate(vehicle.getLicensePlate())
                    .totalExpenseCost(totalExpenseCost)
                    .totalFuelCost(totalFuelCost)
                    .totalMaintenanceCost(totalMaintenanceCost)
                    .totalOperationalCost(totalOperationalCost)
                    .totalDistance(totalDistance)
                    .fuelEfficiency(fuelEfficiency)
                    .costPerKm(costPerKm)
                    .build());
        }
        return responses;
    }

    /**
     * Get top costliest vehicles.
     */
    public List<VehicleCostResponse> getTopCostliestVehicles(int limit) {
        List<VehicleCostResponse> allCosts = getVehicleCosts();
        return allCosts.stream()
                .sorted((a, b) -> b.getTotalOperationalCost().compareTo(a.getTotalOperationalCost()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get overall fleet analytics summary.
     */
    public FleetAnalyticsSummary getFleetAnalyticsSummary() {
        BigDecimal totalFuelCost = expenseRepository.totalFuelCostAll();
        Long totalVehicles = vehicleRepository.countNonRetired();

        // Fleet ROI: (total revenue - total cost) / total acquisition cost
        BigDecimal totalAcquisitionCost = vehicleRepository.findAll().stream()
                .filter(v -> v.getAcquisitionCost() != null)
                .map(Vehicle::getAcquisitionCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = tripRepository.findByStatus(com.hackathon.securestarter.enums.TripStatus.COMPLETED)
                .stream()
                .map(t -> t.getRevenue() != null ? t.getRevenue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenseRepository.findAll().stream()
                .map(e -> e.getTotalCost() != null ? e.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        BigDecimal fleetROI = BigDecimal.ZERO;
        if (totalAcquisitionCost.compareTo(BigDecimal.ZERO) > 0) {
            fleetROI = netProfit.divide(totalAcquisitionCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Utilization rate: vehicles on trip / total non-retired
        Long activeFleet = vehicleRepository.countActiveFleet();
        double utilizationRate = totalVehicles > 0
                ? ((double) activeFleet / totalVehicles) * 100.0
                : 0.0;

        return FleetAnalyticsSummary.builder()
                .totalFuelCost(totalFuelCost)
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .fleetROI(fleetROI)
                .utilizationRate(Math.round(utilizationRate * 100.0) / 100.0)
                .totalVehicles(totalVehicles)
                .build();
    }

    /**
     * Generate current month summary on demand.
     */
    @Transactional
    public MonthlyFinancialSummaryResponse generateCurrentMonthSummary() {
        LocalDate now = LocalDate.now();
        return generateMonthlySummary(now.getYear(), now.getMonthValue());
    }

    // ---- Helper ----

    private MonthlyFinancialSummaryResponse mapToSummaryResponse(MonthlyFinancialSummary summary) {
        return MonthlyFinancialSummaryResponse.builder()
                .year(summary.getYear())
                .month(summary.getMonth())
                .revenue(summary.getRevenue())
                .fuelCost(summary.getFuelCost())
                .maintenanceCost(summary.getMaintenanceCost())
                .netProfit(summary.getNetProfit())
                .totalTrips(summary.getTotalTrips())
                .totalDistance(summary.getTotalDistance())
                .totalFuelLiters(summary.getTotalFuelLiters())
                .build();
    }

    /**
     * Inner DTO for fleet-level analytics summary.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class FleetAnalyticsSummary {
        private BigDecimal totalFuelCost;
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
        private BigDecimal fleetROI;
        private Double utilizationRate;
        private Long totalVehicles;
    }
}
