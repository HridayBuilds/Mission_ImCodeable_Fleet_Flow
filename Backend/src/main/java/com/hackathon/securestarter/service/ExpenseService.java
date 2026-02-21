package com.hackathon.securestarter.service;

import com.hackathon.securestarter.dto.request.CreateExpenseRequest;
import com.hackathon.securestarter.dto.request.UpdateExpenseRequest;
import com.hackathon.securestarter.dto.response.ExpenseResponse;
import com.hackathon.securestarter.entity.Expense;
import com.hackathon.securestarter.entity.Trip;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.enums.ExpenseStatus;
import com.hackathon.securestarter.enums.TripStatus;
import com.hackathon.securestarter.exception.BadRequestException;
import com.hackathon.securestarter.exception.ResourceNotFoundException;
import com.hackathon.securestarter.repository.ExpenseRepository;
import com.hackathon.securestarter.repository.TripRepository;
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
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;

    /**
     * Create an expense record linked to a completed trip.
     * Auto-links vehicle and driver from the trip.
     */
    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request, User currentUser) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException(Constants.TRIP_NOT_FOUND));

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new BadRequestException("Expenses can only be created for COMPLETED trips. Current status: " + trip.getStatus());
        }

        // Check if expense already exists for this trip
        List<Expense> existing = expenseRepository.findByTripId(trip.getId());
        if (!existing.isEmpty()) {
            throw new BadRequestException("An expense record already exists for trip #" + trip.getTripNumber());
        }

        Expense expense = Expense.builder()
                .trip(trip)
                .vehicle(trip.getVehicle())
                .driver(trip.getDriver())
                .distance(request.getDistance() != null ? request.getDistance() : trip.getActualDistance())
                .fuelCost(request.getFuelCost())
                .miscExpense(request.getMiscExpense() != null ? request.getMiscExpense() : BigDecimal.ZERO)
                .status(ExpenseStatus.PENDING)
                .createdBy(currentUser)
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created for trip #{} by user: {}",
                trip.getTripNumber(), currentUser.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public ExpenseResponse updateExpense(UUID expenseId, UpdateExpenseRequest request, User currentUser) {
        Expense expense = getExpenseEntity(expenseId);

        if (expense.getStatus() == ExpenseStatus.DONE) {
            throw new BadRequestException("Cannot update an expense that is already marked as DONE");
        }

        if (request.getDistance() != null) expense.setDistance(request.getDistance());
        if (request.getFuelCost() != null) expense.setFuelCost(request.getFuelCost());
        if (request.getMiscExpense() != null) expense.setMiscExpense(request.getMiscExpense());

        Expense updated = expenseRepository.save(expense);
        log.info("Expense updated: {} by user: {}", expenseId, currentUser.getEmail());
        return mapToResponse(updated);
    }

    @Transactional
    public ExpenseResponse updateExpenseStatus(UUID expenseId, ExpenseStatus newStatus, User currentUser) {
        Expense expense = getExpenseEntity(expenseId);

        if (expense.getStatus() == ExpenseStatus.DONE) {
            throw new BadRequestException("Cannot change status of an expense already marked as DONE");
        }

        expense.setStatus(newStatus);
        Expense updated = expenseRepository.save(expense);
        log.info("Expense {} status changed to {} by user: {}",
                expenseId, newStatus, currentUser.getEmail());
        return mapToResponse(updated);
    }

    public ExpenseResponse getExpenseById(UUID expenseId) {
        return mapToResponse(getExpenseEntity(expenseId));
    }

    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getExpensesByTrip(UUID tripId) {
        return expenseRepository.findByTripId(tripId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getExpensesByVehicle(UUID vehicleId) {
        return expenseRepository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getExpensesByDriver(UUID driverId) {
        return expenseRepository.findByDriverId(driverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getExpensesByStatus(ExpenseStatus status) {
        return expenseRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteExpense(UUID expenseId, User currentUser) {
        Expense expense = getExpenseEntity(expenseId);
        if (expense.getStatus() == ExpenseStatus.DONE) {
            throw new BadRequestException("Cannot delete an expense that is already marked as DONE");
        }
        expenseRepository.delete(expense);
        log.info("Expense deleted: {} by user: {}", expenseId, currentUser.getEmail());
    }

    // ---- Internal helper methods ----

    private Expense getExpenseEntity(UUID expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.EXPENSE_NOT_FOUND));
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        Trip trip = expense.getTrip();
        return ExpenseResponse.builder()
                .id(expense.getId())
                .tripId(trip.getId())
                .tripNumber(trip.getTripNumber())
                .vehicleId(expense.getVehicle().getId())
                .vehicleName(expense.getVehicle().getName())
                .vehicleLicensePlate(expense.getVehicle().getLicensePlate())
                .driverId(expense.getDriver().getId())
                .driverName(expense.getDriver().getName())
                .distance(expense.getDistance())
                .fuelCost(expense.getFuelCost())
                .miscExpense(expense.getMiscExpense())
                .totalCost(expense.getTotalCost())
                .status(expense.getStatus())
                .createdByName(expense.getCreatedBy() != null ? expense.getCreatedBy().getName() : null)
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
