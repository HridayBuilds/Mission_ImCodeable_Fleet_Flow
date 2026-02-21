package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.request.CancelTripRequest;
import com.hackathon.securestarter.dto.request.CompleteTripRequest;
import com.hackathon.securestarter.dto.request.CreateTripRequest;
import com.hackathon.securestarter.dto.response.TripResponse;
import com.hackathon.securestarter.entity.Driver;
import com.hackathon.securestarter.entity.Trip;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.entity.Vehicle;
import com.hackathon.securestarter.enums.DriverStatus;
import com.hackathon.securestarter.enums.TripStatus;
import com.hackathon.securestarter.enums.VehicleStatus;
import com.hackathon.securestarter.exception.BadRequestException;
import com.hackathon.securestarter.exception.ResourceNotFoundException;
import com.hackathon.securestarter.repository.DriverRepository;
import com.hackathon.securestarter.repository.TripRepository;
import com.hackathon.securestarter.repository.VehicleRepository;
import com.hackathon.securestarter.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final DriverService driverService;

    /**
     * Create a new trip in DRAFT status.
     * Validates cargo weight against vehicle capacity.
     * Does NOT change vehicle/driver status yet (that happens on dispatch).
     */
    @Transactional
    public TripResponse createTrip(CreateTripRequest request, User currentUser) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.VEHICLE_NOT_FOUND));
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.DRIVER_NOT_FOUND));

        // Validate vehicle availability
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BadRequestException(Constants.VEHICLE_NOT_AVAILABLE);
        }

        // Validate driver availability
        if (!driver.isAvailableForDispatch()) {
            if (driver.isLicenseExpired()) {
                throw new BadRequestException(Constants.DRIVER_LICENSE_EXPIRED);
            }
            if (driver.getStatus() == DriverStatus.SUSPENDED) {
                throw new BadRequestException(Constants.DRIVER_SUSPENDED);
            }
            throw new BadRequestException(Constants.DRIVER_NOT_AVAILABLE);
        }

        // Validate cargo weight against vehicle capacity
        if (request.getCargoWeight() > vehicle.getMaxLoadCapacity()) {
            throw new BadRequestException(Constants.CARGO_EXCEEDS_CAPACITY +
                    ". Max capacity: " + vehicle.getMaxLoadCapacity() + " kg, Cargo: " + request.getCargoWeight() + " kg");
        }

        // Generate sequential trip number
        Long nextTripNumber = tripRepository.findMaxTripNumber() + 1;

        Trip trip = Trip.builder()
                .tripNumber(nextTripNumber)
                .vehicle(vehicle)
                .driver(driver)
                .cargoWeight(request.getCargoWeight())
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .estimatedFuelCost(request.getEstimatedFuelCost())
                .startOdometer(vehicle.getOdometer())
                .status(TripStatus.DRAFT)
                .dispatchedBy(currentUser)
                .build();

        Trip saved = tripRepository.save(trip);
        log.info("Trip #{} created by user: {}", saved.getTripNumber(), currentUser.getEmail());
        return mapToResponse(saved);
    }

    /**
     * Dispatch a DRAFT trip → DISPATCHED.
     * Sets Vehicle status to ON_TRIP and Driver status to ON_TRIP.
     * Increments driver's totalTripsAssigned.
     */
    @Transactional
    public TripResponse dispatchTrip(UUID tripId, User currentUser) {
        Trip trip = getTripEntity(tripId);

        if (trip.getStatus() != TripStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT trips can be dispatched. Current status: " + trip.getStatus());
        }

        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        // Re-validate availability at dispatch time
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BadRequestException(Constants.VEHICLE_NOT_AVAILABLE + " (status: " + vehicle.getStatus() + ")");
        }
        if (!driver.isAvailableForDispatch()) {
            throw new BadRequestException(Constants.DRIVER_NOT_AVAILABLE + " (status: " + driver.getStatus() + ")");
        }

        // Update statuses
        vehicle.setStatus(VehicleStatus.ON_TRIP);
        driver.setStatus(DriverStatus.ON_TRIP);
        driver.setTotalTripsAssigned(driver.getTotalTripsAssigned() + 1);

        trip.setStatus(TripStatus.DISPATCHED);
        trip.setDispatchedAt(LocalDateTime.now());
        trip.setDispatchedBy(currentUser);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);
        Trip updated = tripRepository.save(trip);

        log.info("Trip #{} dispatched by user: {}", updated.getTripNumber(), currentUser.getEmail());
        return mapToResponse(updated);
    }

    /**
     * Mark a DISPATCHED trip → IN_TRANSIT.
     */
    @Transactional
    public TripResponse markInTransit(UUID tripId, User currentUser) {
        Trip trip = getTripEntity(tripId);

        if (trip.getStatus() != TripStatus.DISPATCHED) {
            throw new BadRequestException("Only DISPATCHED trips can be marked as in-transit. Current status: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.IN_TRANSIT);
        Trip updated = tripRepository.save(trip);

        log.info("Trip #{} marked in-transit by user: {}", updated.getTripNumber(), currentUser.getEmail());
        return mapToResponse(updated);
    }

    /**
     * Complete a trip (DISPATCHED or IN_TRANSIT → COMPLETED).
     * Resets Vehicle to AVAILABLE, Driver to ON_DUTY.
     * Updates vehicle odometer. Increments driver's totalTripsCompleted.
     * Recalculates driver completion rate.
     */
    @Transactional
    public TripResponse completeTrip(UUID tripId, CompleteTripRequest request, User currentUser) {
        Trip trip = getTripEntity(tripId);

        if (trip.getStatus() != TripStatus.DISPATCHED && trip.getStatus() != TripStatus.IN_TRANSIT) {
            throw new BadRequestException("Only DISPATCHED or IN_TRANSIT trips can be completed. Current status: " + trip.getStatus());
        }

        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        // Validate end odometer
        if (request.getEndOdometer() < vehicle.getOdometer()) {
            throw new BadRequestException("End odometer (" + request.getEndOdometer() +
                    ") cannot be less than current odometer (" + vehicle.getOdometer() + ")");
        }

        // Calculate actual distance
        double actualDistance = request.getEndOdometer() - trip.getStartOdometer();

        trip.setEndOdometer(request.getEndOdometer());
        trip.setActualDistance(actualDistance);
        trip.setRevenue(request.getRevenue());
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(LocalDateTime.now());

        // Reset vehicle status and update odometer
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setOdometer(request.getEndOdometer());

        // Reset driver status and update trip stats
        driver.setStatus(DriverStatus.ON_DUTY);
        driver.setTotalTripsCompleted(driver.getTotalTripsCompleted() + 1);
        driverService.recalculateCompletionRate(driver);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);
        Trip updated = tripRepository.save(trip);

        log.info("Trip #{} completed by user: {}. Distance: {} km",
                updated.getTripNumber(), currentUser.getEmail(), actualDistance);
        return mapToResponse(updated);
    }

    /**
     * Cancel a trip (DRAFT, DISPATCHED, or IN_TRANSIT → CANCELLED).
     * If dispatched/in-transit, resets Vehicle to AVAILABLE and Driver to ON_DUTY.
     */
    @Transactional
    public TripResponse cancelTrip(UUID tripId, CancelTripRequest request, User currentUser) {
        Trip trip = getTripEntity(tripId);

        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a trip that is already " + trip.getStatus());
        }

        // If already dispatched or in-transit, release vehicle and driver
        if (trip.getStatus() == TripStatus.DISPATCHED || trip.getStatus() == TripStatus.IN_TRANSIT) {
            Vehicle vehicle = trip.getVehicle();
            Driver driver = trip.getDriver();

            vehicle.setStatus(VehicleStatus.AVAILABLE);
            driver.setStatus(DriverStatus.ON_DUTY);

            vehicleRepository.save(vehicle);
            driverRepository.save(driver);
        }

        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelledAt(LocalDateTime.now());
        trip.setCancellationReason(request != null ? request.getCancellationReason() : null);

        Trip updated = tripRepository.save(trip);
        log.info("Trip #{} cancelled by user: {}", updated.getTripNumber(), currentUser.getEmail());
        return mapToResponse(updated);
    }

    public TripResponse getTripById(UUID tripId) {
        return mapToResponse(getTripEntity(tripId));
    }

    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TripResponse> getTripsByStatus(TripStatus status) {
        return tripRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TripResponse> getTripsByVehicle(UUID vehicleId) {
        return tripRepository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TripResponse> getTripsByDriver(UUID driverId) {
        return tripRepository.findByDriverId(driverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---- Internal helper methods ----

    public Trip getTripEntity(UUID tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.TRIP_NOT_FOUND));
    }

    private TripResponse mapToResponse(Trip trip) {
        Vehicle vehicle = trip.getVehicle();
        Driver driver = trip.getDriver();

        return TripResponse.builder()
                .id(trip.getId())
                .tripNumber(trip.getTripNumber())
                .vehicleId(vehicle.getId())
                .vehicleName(vehicle.getName())
                .vehicleLicensePlate(vehicle.getLicensePlate())
                .driverId(driver.getId())
                .driverName(driver.getName())
                .cargoWeight(trip.getCargoWeight())
                .origin(trip.getOrigin())
                .destination(trip.getDestination())
                .estimatedFuelCost(trip.getEstimatedFuelCost())
                .actualDistance(trip.getActualDistance())
                .startOdometer(trip.getStartOdometer())
                .endOdometer(trip.getEndOdometer())
                .revenue(trip.getRevenue())
                .status(trip.getStatus())
                .dispatchedByName(trip.getDispatchedBy() != null ? trip.getDispatchedBy().getName() : null)
                .dispatchedAt(trip.getDispatchedAt())
                .completedAt(trip.getCompletedAt())
                .cancelledAt(trip.getCancelledAt())
                .cancellationReason(trip.getCancellationReason())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }
}
