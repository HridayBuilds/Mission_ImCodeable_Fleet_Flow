package com.hackathon.securestarter.entity;

import com.hackathon.securestarter.enums.DriverStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Driver entity — represents a fleet driver.
 * Managed primarily by the Safety Officer.
 *
 * Status lifecycle:
 *   ON_DUTY    → ON_TRIP   (when dispatched on a trip)
 *   ON_TRIP    → ON_DUTY   (when trip completed)
 *   ON_DUTY    → OFF_DUTY  (toggle by Safety Officer)
 *   ON_DUTY    → SUSPENDED (by Safety Officer — blocked from dispatch)
 *
 * License Expiry Auto-Logic:
 *   If licenseExpiryDate < today → driver is auto-locked,
 *   Dispatcher cannot see/select this driver.
 *   Safety Officer must renew expiry to unlock.
 */
@Entity
@Table(name = "drivers", indexes = {
        @Index(name = "idx_driver_license_number", columnList = "license_number"),
        @Index(name = "idx_driver_status", columnList = "status"),
        @Index(name = "idx_driver_license_expiry", columnList = "license_expiry_date"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @NotNull
    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;

    @Column(name = "license_category", length = 30)
    private String licenseCategory; // e.g., "Van", "Truck", "Bike"

    @Column(name = "phone", length = 20)
    private String phone;

    @PositiveOrZero
    @Column(name = "completion_rate")
    @Builder.Default
    private Double completionRate = 0.0; // trip completion rate as percentage (0-100)

    @PositiveOrZero
    @Column(name = "safety_score")
    @Builder.Default
    private Double safetyScore = 100.0; // safety score as percentage (0-100)

    @PositiveOrZero
    @Column(name = "complaints")
    @Builder.Default
    private Integer complaints = 0;

    @Column(name = "total_trips_assigned")
    @Builder.Default
    private Integer totalTripsAssigned = 0;

    @Column(name = "total_trips_completed")
    @Builder.Default
    private Integer totalTripsCompleted = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DriverStatus status = DriverStatus.ON_DUTY;

    // Who created this driver record (Safety Officer)
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
     * Check if the driver's license has expired.
     */
    public boolean isLicenseExpired() {
        return licenseExpiryDate != null && LocalDate.now().isAfter(licenseExpiryDate);
    }

    /**
     * Check if driver is available for dispatch:
     * must be ON_DUTY and license must not be expired.
     */
    public boolean isAvailableForDispatch() {
        return status == DriverStatus.ON_DUTY && !isLicenseExpired();
    }
}
