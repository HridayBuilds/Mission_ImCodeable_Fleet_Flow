package com.hackathon.securestarter.entity;

import com.hackathon.securestarter.enums.ExpenseStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Expense entity — tracks fuel and miscellaneous costs per trip.
 * Managed primarily by the Financial Analyst.
 *
 * Auto-links to the Vehicle via the Trip relationship.
 *
 * Total Operational Cost per Vehicle =
 *   sum(Expense.fuelCost + Expense.miscExpense) for all trips of that vehicle
 *   + sum(MaintenanceLog.cost) for that vehicle
 */
@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_trip", columnList = "trip_id"),
        @Index(name = "idx_expense_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_expense_driver", columnList = "driver_id"),
        @Index(name = "idx_expense_status", columnList = "status"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // Auto-linked from the trip's vehicle
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Auto-linked from the trip's driver
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @PositiveOrZero
    @Column(name = "distance")
    private Double distance; // in km

    @PositiveOrZero
    @Column(name = "fuel_cost", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal fuelCost = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(name = "misc_expense", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal miscExpense = BigDecimal.ZERO;

    // Computed: fuelCost + miscExpense (persisted for query performance)
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.PENDING;

    // Who created this expense (Financial Analyst)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Auto-calculate total cost before persisting.
     */
    @PrePersist
    @PreUpdate
    private void calculateTotalCost() {
        BigDecimal fuel = (fuelCost != null) ? fuelCost : BigDecimal.ZERO;
        BigDecimal misc = (miscExpense != null) ? miscExpense : BigDecimal.ZERO;
        this.totalCost = fuel.add(misc);
    }
}
