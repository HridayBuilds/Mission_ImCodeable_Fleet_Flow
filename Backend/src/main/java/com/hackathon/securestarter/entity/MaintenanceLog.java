package com.hackathon.securestarter.entity;

import com.hackathon.securestarter.enums.MaintenanceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MaintenanceLog entity — tracks service and repair records for vehicles.
 * Managed primarily by the Fleet Manager.
 *
 * Auto-Logic:
 *   When a new maintenance log is created for a vehicle,
 *   the vehicle's status automatically switches to IN_SHOP.
 *   The vehicle is then hidden from the Dispatcher's selection pool.
 *
 *   When maintenance is RESOLVED, the vehicle can return to AVAILABLE.
 */
@Entity
@Table(name = "maintenance_logs", indexes = {
        @Index(name = "idx_maintenance_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_maintenance_status", columnList = "status"),
        @Index(name = "idx_maintenance_service_date", columnList = "service_date"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "service_name", length = 200)
    private String serviceName;

    @Column(name = "issue_description", length = 1000)
    private String issueDescription;

    @NotNull
    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @PositiveOrZero
    @Column(name = "cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.NEW;

    // Who created this log (Fleet Manager)
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
