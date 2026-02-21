package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.Driver;
import com.hackathon.securestarter.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    Boolean existsByLicenseNumber(String licenseNumber);

    List<Driver> findByStatus(DriverStatus status);

    /**
     * Find drivers available for dispatch:
     * ON_DUTY status and license not expired.
     */
    @Query("SELECT d FROM Driver d WHERE d.status = 'ON_DUTY' AND d.licenseExpiryDate > :today")
    List<Driver> findAvailableDrivers(@Param("today") LocalDate today);

    /**
     * Find drivers with expired licenses (for Safety Officer alerts).
     */
    @Query("SELECT d FROM Driver d WHERE d.licenseExpiryDate <= :today AND d.status != 'SUSPENDED'")
    List<Driver> findDriversWithExpiredLicense(@Param("today") LocalDate today);

}
