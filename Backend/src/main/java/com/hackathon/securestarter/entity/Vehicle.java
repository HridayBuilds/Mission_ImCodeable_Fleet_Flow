package com.hackathon.securestarter.entity;

import com.hackathon.securestarter.enums.VehicleStatus;
import com.hackathon.securestarter.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vehicle entity — represents a physical fleet asset.
 * Managed primarily by the Fleet Manager.
 *
 * Status lifecycle:
 *   AVAILABLE → ON_TRIP (when dispatched)
 *   AVAILABLE → IN_SHOP (when maintenance logged)
 *   ON_TRIP   → AVAILABLE (when trip completed)
 *   IN_SHOP   → AVAILABLE (when maintenance resolved)
 *   Any       → RETIRED  (manual toggle by Fleet Manager)
 */
@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_license_plate", columnList = "license_plate"),
        @Index(name = "idx_vehicle_status", columnList = "status"),
        @Index(name = "idx_vehicle_type", columnList = "type"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(name = "license_plate", nullable = false, unique = true, length = 30)
    private String licensePlate;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "model", length = 100)
    private String model;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private VehicleType type;

    @NotNull
    @PositiveOrZero
    @Column(name = "max_load_capacity", nullable = false)
    private Double maxLoadCapacity; // in kg

    @PositiveOrZero
    @Column(name = "odometer", nullable = false)
    @Builder.Default
    private Double odometer = 0.0; // in km

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @PositiveOrZero
    @Column(name = "acquisition_cost", precision = 12, scale = 2)
    private BigDecimal acquisitionCost;

    // Who registered this vehicle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
