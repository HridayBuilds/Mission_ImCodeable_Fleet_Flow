package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.request.CreateVehicleRequest;
import com.hackathon.securestarter.dto.request.UpdateVehicleRequest;
import com.hackathon.securestarter.dto.response.VehicleResponse;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.entity.Vehicle;
import com.hackathon.securestarter.enums.VehicleStatus;
import com.hackathon.securestarter.enums.VehicleType;
import com.hackathon.securestarter.exception.BadRequestException;
import com.hackathon.securestarter.exception.ResourceNotFoundException;
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
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest request, User currentUser) {
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new BadRequestException(Constants.LICENSE_PLATE_ALREADY_EXISTS);
        }

        Vehicle vehicle = Vehicle.builder()
                .licensePlate(request.getLicensePlate().toUpperCase().trim())
                .name(request.getName())
                .model(request.getModel())
                .type(request.getType())
                .maxLoadCapacity(request.getMaxLoadCapacity())
                .odometer(request.getOdometer() != null ? request.getOdometer() : 0.0)
                .acquisitionCost(request.getAcquisitionCost())
                .status(VehicleStatus.AVAILABLE)
                .createdBy(currentUser)
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created: {} by user: {}", saved.getLicensePlate(), currentUser.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID vehicleId, UpdateVehicleRequest request, User currentUser) {
        Vehicle vehicle = getVehicleEntity(vehicleId);

        if (request.getName() != null) vehicle.setName(request.getName());
        if (request.getModel() != null) vehicle.setModel(request.getModel());
        if (request.getType() != null) vehicle.setType(request.getType());
        if (request.getMaxLoadCapacity() != null) vehicle.setMaxLoadCapacity(request.getMaxLoadCapacity());
        if (request.getAcquisitionCost() != null) vehicle.setAcquisitionCost(request.getAcquisitionCost());

        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Vehicle updated: {} by user: {}", updated.getLicensePlate(), currentUser.getEmail());
        return mapToResponse(updated);
    }

    @Transactional
    public VehicleResponse updateVehicleStatus(UUID vehicleId, VehicleStatus newStatus, User currentUser) {
        Vehicle vehicle = getVehicleEntity(vehicleId);
        VehicleStatus oldStatus = vehicle.getStatus();

        // Validate status transitions
        validateStatusTransition(oldStatus, newStatus);

        vehicle.setStatus(newStatus);
        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Vehicle {} status changed: {} → {} by user: {}",
                updated.getLicensePlate(), oldStatus, newStatus, currentUser.getEmail());
        return mapToResponse(updated);
    }

    public VehicleResponse getVehicleById(UUID vehicleId) {
        return mapToResponse(getVehicleEntity(vehicleId));
    }

    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<VehicleResponse> getVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<VehicleResponse> getVehiclesByType(VehicleType type) {
        return vehicleRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available vehicles for dispatch (AVAILABLE status, non-retired).
     */
    public List<VehicleResponse> getAvailableVehicles() {
        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVehicle(UUID vehicleId, User currentUser) {
        Vehicle vehicle = getVehicleEntity(vehicleId);
        if (vehicle.getStatus() == VehicleStatus.ON_TRIP) {
            throw new BadRequestException("Cannot delete a vehicle that is currently on a trip");
        }
        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted: {} by user: {}", vehicle.getLicensePlate(), currentUser.getEmail());
    }

    // ---- Internal helper methods ----

    public Vehicle getVehicleEntity(UUID vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.VEHICLE_NOT_FOUND));
    }

    private void validateStatusTransition(VehicleStatus from, VehicleStatus to) {
        if (from == VehicleStatus.RETIRED && to != VehicleStatus.RETIRED) {
            throw new BadRequestException("Cannot change status of a retired vehicle");
        }
        if (from == VehicleStatus.ON_TRIP && to != VehicleStatus.AVAILABLE && to != VehicleStatus.RETIRED) {
            throw new BadRequestException("Vehicle on trip can only be set to AVAILABLE (on trip completion) or RETIRED");
        }
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .name(vehicle.getName())
                .model(vehicle.getModel())
                .type(vehicle.getType())
                .maxLoadCapacity(vehicle.getMaxLoadCapacity())
                .odometer(vehicle.getOdometer())
                .status(vehicle.getStatus())
                .acquisitionCost(vehicle.getAcquisitionCost())
                .createdByName(vehicle.getCreatedBy() != null ? vehicle.getCreatedBy().getName() : null)
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
