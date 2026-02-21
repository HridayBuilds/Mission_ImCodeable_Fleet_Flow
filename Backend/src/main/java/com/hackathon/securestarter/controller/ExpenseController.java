package com.hackathon.securestarter.controller;

import com.hackathon.securestarter.dto.request.CreateExpenseRequest;
import com.hackathon.securestarter.dto.request.UpdateExpenseRequest;
import com.hackathon.securestarter.dto.response.ExpenseResponse;
import com.hackathon.securestarter.dto.response.MessageResponse;
import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.enums.ExpenseStatus;
import com.hackathon.securestarter.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    // ===== WRITE Operations (FINANCIAL_ANALYST only) =====

    @PostMapping
    @PreAuthorize("hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Create expense by user: {}", currentUser.getEmail());
        ExpenseResponse response = expenseService.createExpense(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExpenseRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Update expense {} by user: {}", id, currentUser.getEmail());
        ExpenseResponse response = expenseService.updateExpense(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<ExpenseResponse> updateExpenseStatus(
            @PathVariable UUID id,
            @RequestParam ExpenseStatus status,
            @AuthenticationPrincipal User currentUser) {
        log.info("Update expense {} status to {} by user: {}", id, status, currentUser.getEmail());
        ExpenseResponse response = expenseService.updateExpenseStatus(id, status, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<MessageResponse> deleteExpense(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        log.info("Delete expense {} by user: {}", id, currentUser.getEmail());
        expenseService.deleteExpense(id, currentUser);
        return ResponseEntity.ok(MessageResponse.success("Expense deleted successfully"));
    }

    // ===== READ Operations (FINANCIAL_ANALYST and FLEET_MANAGER) =====

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FINANCIAL_ANALYST', 'FLEET_MANAGER')")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable UUID id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCIAL_ANALYST', 'FLEET_MANAGER')")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
            @RequestParam(required = false) UUID tripId,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) ExpenseStatus status) {
        List<ExpenseResponse> responses;
        if (tripId != null) {
            responses = expenseService.getExpensesByTrip(tripId);
        } else if (vehicleId != null) {
            responses = expenseService.getExpensesByVehicle(vehicleId);
        } else if (driverId != null) {
            responses = expenseService.getExpensesByDriver(driverId);
        } else if (status != null) {
            responses = expenseService.getExpensesByStatus(status);
        } else {
            responses = expenseService.getAllExpenses();
        }
        return ResponseEntity.ok(responses);
    }
}
