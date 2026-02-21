package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.Expense;
import com.hackathon.securestarter.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByTripId(UUID tripId);

    List<Expense> findByVehicleId(UUID vehicleId);

    List<Expense> findByDriverId(UUID driverId);

    List<Expense> findByStatus(ExpenseStatus status);

    /**
     * Total operational cost (fuel + misc) for a specific vehicle.
     */
    @Query("SELECT COALESCE(SUM(e.totalCost), 0) FROM Expense e WHERE e.vehicle.id = :vehicleId")
    BigDecimal totalCostByVehicleId(@Param("vehicleId") UUID vehicleId);

    /**
     * Total fuel cost for a specific vehicle.
     */
    @Query("SELECT COALESCE(SUM(e.fuelCost), 0) FROM Expense e WHERE e.vehicle.id = :vehicleId")
    BigDecimal totalFuelCostByVehicleId(@Param("vehicleId") UUID vehicleId);

    /**
     * Total fuel cost across all vehicles.
     */
    @Query("SELECT COALESCE(SUM(e.fuelCost), 0) FROM Expense e")
    BigDecimal totalFuelCostAll();

    /**
     * Top N costliest vehicles by total expense.
     */
    @Query("SELECT e.vehicle.id, SUM(e.totalCost) as total FROM Expense e " +
           "GROUP BY e.vehicle.id ORDER BY total DESC")
    List<Object[]> findTopCostliestVehicles();

}
