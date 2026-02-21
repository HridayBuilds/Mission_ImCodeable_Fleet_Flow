package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.Trip;
import com.hackathon.securestarter.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    Optional<Trip> findByTripNumber(Long tripNumber);

    List<Trip> findByStatus(TripStatus status);

    List<Trip> findByVehicleId(UUID vehicleId);

    List<Trip> findByDriverId(UUID driverId);

    List<Trip> findByDispatchedById(UUID userId);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = 'DRAFT'")
    Long countPendingCargo();

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = 'DISPATCHED' OR t.status = 'IN_TRANSIT'")
    Long countActiveTrips();

    @Query("SELECT COALESCE(MAX(t.tripNumber), 0) FROM Trip t")
    Long findMaxTripNumber();

    /**
     * Find completed trips for a vehicle (for expense linking).
     */
    List<Trip> findByVehicleIdAndStatus(UUID vehicleId, TripStatus status);

    /**
     * Find trips by vehicle and multiple statuses.
     */
    @Query("SELECT t FROM Trip t WHERE t.vehicle.id = :vehicleId AND t.status IN :statuses")
    List<Trip> findByVehicleIdAndStatusIn(@Param("vehicleId") UUID vehicleId,
                                          @Param("statuses") List<TripStatus> statuses);

}
