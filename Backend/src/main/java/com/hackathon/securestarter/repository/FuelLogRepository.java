package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.FuelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface FuelLogRepository extends JpaRepository<FuelLog, UUID> {

    List<FuelLog> findByVehicleId(UUID vehicleId);

    List<FuelLog> findByTripId(UUID tripId);

    /**
     * Total fuel cost for a vehicle.
     */
    @Query("SELECT COALESCE(SUM(f.cost), 0) FROM FuelLog f WHERE f.vehicle.id = :vehicleId")
    BigDecimal totalFuelCostByVehicleId(@Param("vehicleId") UUID vehicleId);

    /**
     * Total liters for a vehicle (for km/L efficiency calculation).
     */
    @Query("SELECT COALESCE(SUM(f.liters), 0) FROM FuelLog f WHERE f.vehicle.id = :vehicleId")
    Double totalLitersByVehicleId(@Param("vehicleId") UUID vehicleId);

}
