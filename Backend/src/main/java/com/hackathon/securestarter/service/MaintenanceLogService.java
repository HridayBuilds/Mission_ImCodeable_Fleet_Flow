package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.request.CreateMaintenanceLogRequest;
import com.hackathon.securestarter.dto.request.UpdateMaintenanceLogRequest;
import com.hackathon.securestarter.dto.response.MaintenanceLogResponse;
import com.hackathon.securestarter.entity.MaintenanceLog;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.entity.Vehicle;
import com.hackathon.securestarter.enums.MaintenanceStatus;
import com.hackathon.securestarter.enums.VehicleStatus;
import com.hackathon.securestarter.exception.BadRequestException;
import com.hackathon.securestarter.exception.ResourceNotFoundException;
import com.hackathon.securestarter.repository.MaintenanceLogRepository;
import com.hackathon.securestarter.repository.VehicleRepository;
import com.hackathon.securestarter.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceLogService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * Create a maintenance log.
     * Auto-sets vehicle status to IN_SHOP (hides from dispatcher pool).
     */
    @Transactional
    public MaintenanceLogResponse createMaintenanceLog(CreateMaintenanceLogRequest request, User currentUser) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.VEHICLE_NOT_FOUND));

        if (vehicle.getStatus() == VehicleStatus.ON_TRIP) {
            throw new BadRequestException("Cannot create maintenance log for a vehicle currently on a trip");
        }

        MaintenanceLog maintenanceLog = MaintenanceLog.builder()
                .vehicle(vehicle)
                .serviceName(request.getServiceName())
                .issueDescription(request.getIssueDescription())
                .serviceDate(request.getServiceDate())
                .cost(request.getCost() != null ? request.getCost() : BigDecimal.ZERO)
                .status(MaintenanceStatus.NEW)
                .createdBy(currentUser)
                .build();

        // Auto-set vehicle to IN_SHOP
        vehicle.setStatus(VehicleStatus.IN_SHOP);
        vehicleRepository.save(vehicle);

        MaintenanceLog saved = maintenanceLogRepository.save(maintenanceLog);
        log.info("Maintenance log created for vehicle: {} by user: {}",
                vehicle.getLicensePlate(), currentUser.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public MaintenanceLogResponse updateMaintenanceLog(UUID logId, UpdateMaintenanceLogRequest request, User currentUser) {
        MaintenanceLog maintenanceLog = getMaintenanceLogEntity(logId);

        if (request.getServiceName() != null) maintenanceLog.setServiceName(request.getServiceName());
        if (request.getIssueDescription() != null) maintenanceLog.setIssueDescription(request.getIssueDescription());
        if (request.getServiceDate() != null) maintenanceLog.setServiceDate(request.getServiceDate());
        if (request.getCost() != null) maintenanceLog.setCost(request.getCost());

        MaintenanceLog updated = maintenanceLogRepository.save(maintenanceLog);
        log.info("Maintenance log updated: {} by user: {}", logId, currentUser.getEmail());
        return mapToResponse(updated);
    }

    /**
     * Update maintenance status.
     * When RESOLVED → set vehicle back to AVAILABLE (if no other open maintenance logs).
     */
    @Transactional
    public MaintenanceLogResponse updateMaintenanceStatus(UUID logId, MaintenanceStatus newStatus, User currentUser) {
        MaintenanceLog maintenanceLog = getMaintenanceLogEntity(logId);
        MaintenanceStatus oldStatus = maintenanceLog.getStatus();

        if (oldStatus == MaintenanceStatus.RESOLVED) {
            throw new BadRequestException("Cannot change status of a resolved maintenance log");
        }

        maintenanceLog.setStatus(newStatus);
        MaintenanceLog updated = maintenanceLogRepository.save(maintenanceLog);

        // If resolved, check if vehicle can go back to AVAILABLE
        if (newStatus == MaintenanceStatus.RESOLVED) {
            Vehicle vehicle = maintenanceLog.getVehicle();
            // Check if there are any other open (non-resolved) maintenance logs for this vehicle
            List<MaintenanceLog> openLogs = maintenanceLogRepository
                    .findByVehicleIdAndStatus(vehicle.getId(), MaintenanceStatus.NEW);
            List<MaintenanceLog> inProgressLogs = maintenanceLogRepository
                    .findByVehicleIdAndStatus(vehicle.getId(), MaintenanceStatus.IN_PROGRESS);

            if (openLogs.isEmpty() && inProgressLogs.isEmpty()) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle);
                log.info("Vehicle {} returned to AVAILABLE after all maintenance resolved", vehicle.getLicensePlate());
            }
        }

        log.info("Maintenance log {} status changed: {} → {} by user: {}",
                logId, oldStatus, newStatus, currentUser.getEmail());
        return mapToResponse(updated);
    }

    public MaintenanceLogResponse getMaintenanceLogById(UUID logId) {
        return mapToResponse(getMaintenanceLogEntity(logId));
    }

    public List<MaintenanceLogResponse> getAllMaintenanceLogs() {
        return maintenanceLogRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MaintenanceLogResponse> getMaintenanceLogsByVehicle(UUID vehicleId) {
        return maintenanceLogRepository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MaintenanceLogResponse> getMaintenanceLogsByStatus(MaintenanceStatus status) {
        return maintenanceLogRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMaintenanceLog(UUID logId, User currentUser) {
        MaintenanceLog maintenanceLog = getMaintenanceLogEntity(logId);
        maintenanceLogRepository.delete(maintenanceLog);
        log.info("Maintenance log deleted: {} by user: {}", logId, currentUser.getEmail());
    }

    // ---- Internal helper methods ----

    private MaintenanceLog getMaintenanceLogEntity(UUID logId) {
        return maintenanceLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MAINTENANCE_LOG_NOT_FOUND));
    }

    private MaintenanceLogResponse mapToResponse(MaintenanceLog log) {
        Vehicle vehicle = log.getVehicle();
        return MaintenanceLogResponse.builder()
                .id(log.getId())
                .vehicleId(vehicle.getId())
                .vehicleName(vehicle.getName())
                .vehicleLicensePlate(vehicle.getLicensePlate())
                .serviceName(log.getServiceName())
                .issueDescription(log.getIssueDescription())
                .serviceDate(log.getServiceDate())
                .cost(log.getCost())
                .status(log.getStatus())
                .createdByName(log.getCreatedBy() != null ? log.getCreatedBy().getName() : null)
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}
