package com.leaveexpense.leave_expense_backend.controller;


import com.leaveexpense.leave_expense_backend.dto.ExpenseDtos;
import com.leaveexpense.leave_expense_backend.dto.LeaveDtos;
import com.leaveexpense.leave_expense_backend.security.UserPrincipal;
import com.leaveexpense.leave_expense_backend.service.ExpenseClaimService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expense-claims")
public class ExpenseClaimController {

    private final ExpenseClaimService expenseClaimService;

    public ExpenseClaimController(ExpenseClaimService expenseClaimService) {
        this.expenseClaimService = expenseClaimService;
    }

    @PostMapping
    public ResponseEntity<ExpenseDtos.ExpenseResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                              @Valid @RequestBody ExpenseDtos.CreateExpenseRequest request) {
        return ResponseEntity.ok(expenseClaimService.createExpenseClaim(principal, request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ExpenseDtos.ExpenseResponse>> getMyClaims(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(expenseClaimService.getMyExpenseClaims(principal.getId()));
    }

    @GetMapping("/pending-for-team")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<ExpenseDtos.ExpenseResponse>> getPendingForTeam(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(expenseClaimService.getPendingClaimsForManager(principal.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExpenseDtos.ExpenseResponse>> getAll() {
        return ResponseEntity.ok(expenseClaimService.getAllClaimsForAdmin());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ExpenseDtos.ExpenseResponse> approve(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PathVariable Long id,
                                                               @RequestBody(required = false) LeaveDtos.ActionRequest request) {
        String comment = request != null ? request.getComment() : null;
        return ResponseEntity.ok(expenseClaimService.approveClaim(principal, id, comment));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ExpenseDtos.ExpenseResponse> reject(@AuthenticationPrincipal UserPrincipal principal,
                                                              @PathVariable Long id,
                                                              @RequestBody(required = false) LeaveDtos.ActionRequest request) {
        String comment = request != null ? request.getComment() : null;
        return ResponseEntity.ok(expenseClaimService.rejectClaim(principal, id, comment));
    }
}