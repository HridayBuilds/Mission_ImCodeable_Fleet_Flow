package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.Vehicle;
import com.hackathon.securestarter.enums.VehicleStatus;
import com.hackathon.securestarter.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    Boolean existsByLicensePlate(String licensePlate);

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByType(VehicleType type);

    List<Vehicle> findByStatusAndType(VehicleStatus status, VehicleType type);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = 'ON_TRIP'")
    Long countActiveFleet();

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = 'IN_SHOP'")
    Long countInShop();

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status != 'RETIRED'")
    Long countNonRetired();

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = 'ON_TRIP' OR v.status = 'IN_SHOP'")
    Long countAssigned();

}
