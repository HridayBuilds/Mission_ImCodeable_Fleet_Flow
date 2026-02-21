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
 * FuelLog entity — individual fuel fill-up records per vehicle.
 * Linked to a specific trip or standalone refueling event.
 * Used for calculating fuel efficiency (km/L) and cost-per-km analytics.
 */
@Entity
@Table(name = "fuel_logs", indexes = {
        @Index(name = "idx_fuel_log_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_fuel_log_trip", columnList = "trip_id"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip; // optional — may be standalone refuel

    @NotNull
    @Column(name = "liters", nullable = false)
    private Double liters;

    @NotNull
    @Column(name = "cost", precision = 10, scale = 2, nullable = false)
    private BigDecimal cost;

    @Column(name = "odometer_at_fill")
    private Double odometerAtFill;

    @Column(name = "fill_date", nullable = false)
    private LocalDateTime fillDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
