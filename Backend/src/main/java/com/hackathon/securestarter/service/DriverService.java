package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.request.CreateDriverRequest;
import com.hackathon.securestarter.dto.request.UpdateDriverRequest;
import com.hackathon.securestarter.dto.response.DriverResponse;
import com.hackathon.securestarter.entity.Driver;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.enums.DriverStatus;
import com.hackathon.securestarter.exception.BadRequestException;
import com.hackathon.securestarter.exception.ResourceNotFoundException;
import com.hackathon.securestarter.repository.DriverRepository;
import com.hackathon.securestarter.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public DriverResponse createDriver(CreateDriverRequest request, User currentUser) {
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BadRequestException(Constants.LICENSE_NUMBER_ALREADY_EXISTS);
        }

        Driver driver = Driver.builder()
                .name(request.getName())
                .licenseNumber(request.getLicenseNumber().toUpperCase().trim())
                .licenseExpiryDate(request.getLicenseExpiryDate())
                .licenseCategory(request.getLicenseCategory())
                .phone(request.getPhone())
                .status(DriverStatus.ON_DUTY)
                .createdBy(currentUser)
                .build();

        Driver saved = driverRepository.save(driver);
        log.info("Driver created: {} by user: {}", saved.getName(), currentUser.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public DriverResponse updateDriver(UUID driverId, UpdateDriverRequest request, User currentUser) {
        Driver driver = getDriverEntity(driverId);

        if (request.getName() != null) driver.setName(request.getName());
        if (request.getLicenseExpiryDate() != null) driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
        if (request.getLicenseCategory() != null) driver.setLicenseCategory(request.getLicenseCategory());
        if (request.getPhone() != null) driver.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            validateDriverStatusChange(driver, request.getStatus());
            driver.setStatus(request.getStatus());
        }

        Driver updated = driverRepository.save(driver);
        log.info("Driver updated: {} by user: {}", updated.getName(), currentUser.getEmail());
        return mapToResponse(updated);
    }

    @Transactional
    public DriverResponse updateDriverStatus(UUID driverId, DriverStatus newStatus, User currentUser) {
        Driver driver = getDriverEntity(driverId);
        validateDriverStatusChange(driver, newStatus);
        driver.setStatus(newStatus);
        Driver updated = driverRepository.save(driver);
        log.info("Driver {} status changed to {} by user: {}", updated.getName(), newStatus, currentUser.getEmail());
        return mapToResponse(updated);
    }

    @Transactional
    public DriverResponse addComplaint(UUID driverId, User currentUser) {
        Driver driver = getDriverEntity(driverId);
        driver.setComplaints(driver.getComplaints() + 1);
        // Reduce safety score by 5 per complaint, minimum 0
        double newScore = Math.max(0, driver.getSafetyScore() - 5.0);
        driver.setSafetyScore(newScore);
        Driver updated = driverRepository.save(driver);
        log.info("Complaint added to driver: {} by user: {}", updated.getName(), currentUser.getEmail());
        return mapToResponse(updated);
    }

    public DriverResponse getDriverById(UUID driverId) {
        return mapToResponse(getDriverEntity(driverId));
    }

    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DriverResponse> getDriversByStatus(DriverStatus status) {
        return driverRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get drivers available for dispatch:
     * ON_DUTY status + non-expired license.
     */
    public List<DriverResponse> getAvailableDrivers() {
        return driverRepository.findAvailableDrivers(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get drivers with expired licenses (Safety Officer alert).
     */
    public List<DriverResponse> getDriversWithExpiredLicense() {
        return driverRepository.findDriversWithExpiredLicense(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDriver(UUID driverId, User currentUser) {
        Driver driver = getDriverEntity(driverId);
        if (driver.getStatus() == DriverStatus.ON_TRIP) {
            throw new BadRequestException("Cannot delete a driver that is currently on a trip");
        }
        driverRepository.delete(driver);
        log.info("Driver deleted: {} by user: {}", driver.getName(), currentUser.getEmail());
    }

    // ---- Internal helper methods ----

    public Driver getDriverEntity(UUID driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.DRIVER_NOT_FOUND));
    }

    private void validateDriverStatusChange(Driver driver, DriverStatus newStatus) {
        if (driver.getStatus() == DriverStatus.ON_TRIP && newStatus != DriverStatus.ON_DUTY) {
            throw new BadRequestException("Driver on trip can only be set back to ON_DUTY (on trip completion)");
        }
        if (driver.isLicenseExpired() && newStatus == DriverStatus.ON_DUTY) {
            throw new BadRequestException(Constants.DRIVER_LICENSE_EXPIRED +
                    ". Renew the license expiry date before setting status to ON_DUTY");
        }
    }

    /**
     * Recalculate the completion rate for a driver.
     */
    public void recalculateCompletionRate(Driver driver) {
        if (driver.getTotalTripsAssigned() > 0) {
            double rate = ((double) driver.getTotalTripsCompleted() / driver.getTotalTripsAssigned()) * 100.0;
            driver.setCompletionRate(Math.round(rate * 100.0) / 100.0);
        }
    }

    private DriverResponse mapToResponse(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .name(driver.getName())
                .licenseNumber(driver.getLicenseNumber())
                .licenseExpiryDate(driver.getLicenseExpiryDate())
                .licenseCategory(driver.getLicenseCategory())
                .phone(driver.getPhone())
                .completionRate(driver.getCompletionRate())
                .safetyScore(driver.getSafetyScore())
                .complaints(driver.getComplaints())
                .totalTripsAssigned(driver.getTotalTripsAssigned())
                .totalTripsCompleted(driver.getTotalTripsCompleted())
                .status(driver.getStatus())
                .licenseExpired(driver.isLicenseExpired())
                .createdByName(driver.getCreatedBy() != null ? driver.getCreatedBy().getName() : null)
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }
}
