package com.hackathon.securestarter.entity;

import com.hackathon.securestarter.enums.TripStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Trip entity — represents a cargo delivery trip.
 * Created primarily by the Dispatcher.
 *
 * Lifecycle: DRAFT → DISPATCHED → IN_TRANSIT → COMPLETED / CANCELLED
 *
 * Validation rules:
 *   - CargoWeight must NOT exceed Vehicle.maxLoadCapacity
 *   - Vehicle must have status AVAILABLE at dispatch time
 *   - Driver must be ON_DUTY with a valid (non-expired) license
 *
 * On dispatch:
 *   Vehicle.status → ON_TRIP, Driver.status → ON_TRIP
 *
 * On completion:
 *   Vehicle.status → AVAILABLE, Driver.status → ON_DUTY
 *   Trip data flows into Expense & Fuel page
 */
@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_status", columnList = "status"),
        @Index(name = "idx_trip_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_trip_driver", columnList = "driver_id"),
        @Index(name = "idx_trip_dispatched_by", columnList = "dispatched_by"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "trip_number", unique = true)
    private Long tripNumber; // auto-generated sequential number

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @NotNull
    @Positive
    @Column(name = "cargo_weight", nullable = false)
    private Double cargoWeight; // in kg

    @NotBlank
    @Column(name = "origin", nullable = false, length = 255)
    private String origin;

    @NotBlank
    @Column(name = "destination", nullable = false, length = 255)
    private String destination;

    @PositiveOrZero
    @Column(name = "estimated_fuel_cost", precision = 10, scale = 2)
    private BigDecimal estimatedFuelCost;

    @PositiveOrZero
    @Column(name = "actual_distance")
    private Double actualDistance; // in km, filled on completion

    @Column(name = "start_odometer")
    private Double startOdometer;

    @Column(name = "end_odometer")
    private Double endOdometer; // filled on completion

    @Column(name = "revenue", precision = 12, scale = 2)
    private BigDecimal revenue; // income from this trip

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TripStatus status = TripStatus.DRAFT;

    // Who dispatched this trip (Dispatcher user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatched_by")
    private User dispatchedBy;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
