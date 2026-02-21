package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.MaintenanceLog;
import com.hackathon.securestarter.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, UUID> {

    List<MaintenanceLog> findByVehicleId(UUID vehicleId);

    List<MaintenanceLog> findByStatus(MaintenanceStatus status);

    List<MaintenanceLog> findByVehicleIdAndStatus(UUID vehicleId, MaintenanceStatus status);

    /**
     * Total maintenance cost for a specific vehicle.
     */
    @Query("SELECT COALESCE(SUM(m.cost), 0) FROM MaintenanceLog m WHERE m.vehicle.id = :vehicleId")
    BigDecimal totalCostByVehicleId(@Param("vehicleId") UUID vehicleId);

    /**
     * Total maintenance cost across all vehicles for a given month/year.
     */
    @Query("SELECT COALESCE(SUM(m.cost), 0) FROM MaintenanceLog m " +
           "WHERE YEAR(m.serviceDate) = :year AND MONTH(m.serviceDate) = :month")
    BigDecimal totalCostByMonth(@Param("year") int year, @Param("month") int month);

}
