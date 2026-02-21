package com.hackathon.securestarter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MonthlyFinancialSummary entity — aggregated monthly financial data
 * for the Analytics & Reports page.
 *
 * Used by the Financial Analyst to view:
 *   - Revenue, Fuel Cost, Maintenance Cost, Net Profit per month
 *   - Fleet ROI calculations
 *   - One-click export to CSV/PDF
 */
@Entity
@Table(name = "monthly_financial_summaries", indexes = {
        @Index(name = "idx_financial_summary_period", columnList = "year, month"),
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_financial_summary_month_year", columnNames = {"year", "month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyFinancialSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull
    @Column(name = "month", nullable = false)
    private Integer month; // 1-12

    @Column(name = "revenue", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(name = "fuel_cost", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal fuelCost = BigDecimal.ZERO;

    @Column(name = "maintenance_cost", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal maintenanceCost = BigDecimal.ZERO;

    @Column(name = "net_profit", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal netProfit = BigDecimal.ZERO;

    @Column(name = "total_trips")
    @Builder.Default
    private Integer totalTrips = 0;

    @Column(name = "total_distance")
    @Builder.Default
    private Double totalDistance = 0.0; // total km

    @Column(name = "total_fuel_liters")
    @Builder.Default
    private Double totalFuelLiters = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Recalculate net profit: Revenue - (FuelCost + MaintenanceCost)
     */
    @PrePersist
    @PreUpdate
    private void calculateNetProfit() {
        BigDecimal rev = (revenue != null) ? revenue : BigDecimal.ZERO;
        BigDecimal fuel = (fuelCost != null) ? fuelCost : BigDecimal.ZERO;
        BigDecimal maint = (maintenanceCost != null) ? maintenanceCost : BigDecimal.ZERO;
        this.netProfit = rev.subtract(fuel).subtract(maint);
    }
}
