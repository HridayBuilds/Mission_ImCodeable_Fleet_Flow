package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.request.CreateFuelLogRequest;
import com.hackathon.securestarter.dto.response.FuelLogResponse;
import com.hackathon.securestarter.entity.FuelLog;
import com.hackathon.securestarter.entity.Trip;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.entity.Vehicle;
import com.hackathon.securestarter.exception.ResourceNotFoundException;
import com.hackathon.securestarter.repository.FuelLogRepository;
import com.hackathon.securestarter.repository.TripRepository;
import com.hackathon.securestarter.repository.VehicleRepository;
import com.hackathon.securestarter.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FuelLogService {

    private final FuelLogRepository fuelLogRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;

    @Transactional
    public FuelLogResponse createFuelLog(CreateFuelLogRequest request, User currentUser) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.VEHICLE_NOT_FOUND));

        Trip trip = null;
        if (request.getTripId() != null) {
            trip = tripRepository.findById(request.getTripId())
                    .orElseThrow(() -> new ResourceNotFoundException(Constants.TRIP_NOT_FOUND));
        }

        FuelLog fuelLog = FuelLog.builder()
                .vehicle(vehicle)
                .trip(trip)
                .liters(request.getLiters())
                .cost(request.getCost())
                .odometerAtFill(request.getOdometerAtFill())
                .fillDate(request.getFillDate())
                .recordedBy(currentUser)
                .build();

        FuelLog saved = fuelLogRepository.save(fuelLog);
        log.info("Fuel log created for vehicle: {} by user: {}",
                vehicle.getLicensePlate(), currentUser.getEmail());
        return mapToResponse(saved);
    }

    public FuelLogResponse getFuelLogById(UUID fuelLogId) {
        FuelLog fuelLog = fuelLogRepository.findById(fuelLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel log not found"));
        return mapToResponse(fuelLog);
    }

    public List<FuelLogResponse> getAllFuelLogs() {
        return fuelLogRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FuelLogResponse> getFuelLogsByVehicle(UUID vehicleId) {
        return fuelLogRepository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FuelLogResponse> getFuelLogsByTrip(UUID tripId) {
        return fuelLogRepository.findByTripId(tripId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFuelLog(UUID fuelLogId, User currentUser) {
        FuelLog fuelLog = fuelLogRepository.findById(fuelLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuel log not found"));
        fuelLogRepository.delete(fuelLog);
        log.info("Fuel log deleted: {} by user: {}", fuelLogId, currentUser.getEmail());
    }

    private FuelLogResponse mapToResponse(FuelLog fuelLog) {
        Vehicle vehicle = fuelLog.getVehicle();
        Trip trip = fuelLog.getTrip();

        return FuelLogResponse.builder()
                .id(fuelLog.getId())
                .vehicleId(vehicle.getId())
                .vehicleName(vehicle.getName())
                .vehicleLicensePlate(vehicle.getLicensePlate())
                .tripId(trip != null ? trip.getId() : null)
                .tripNumber(trip != null ? trip.getTripNumber() : null)
                .liters(fuelLog.getLiters())
                .cost(fuelLog.getCost())
                .odometerAtFill(fuelLog.getOdometerAtFill())
                .fillDate(fuelLog.getFillDate())
                .recordedByName(fuelLog.getRecordedBy() != null ? fuelLog.getRecordedBy().getName() : null)
                .createdAt(fuelLog.getCreatedAt())
                .build();
    }
}
