package com.leaveexpense.leave_expense_backend.service;


import com.leaveexpense.leave_expense_backend.Repository.ExpenseClaimRepository;
import com.leaveexpense.leave_expense_backend.dto.ExpenseDtos;
import com.leaveexpense.leave_expense_backend.entity.ExpenseClaim;
import com.leaveexpense.leave_expense_backend.entity.RequestStatus;
import com.leaveexpense.leave_expense_backend.entity.Role;
import com.leaveexpense.leave_expense_backend.entity.User;
import com.leaveexpense.leave_expense_backend.exception.ResourceNotFoundException;
import com.leaveexpense.leave_expense_backend.exception.UnauthorizedActionException;
import com.leaveexpense.leave_expense_backend.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExpenseClaimService {

    private final ExpenseClaimRepository expenseClaimRepository;
    private final UserService userService;

    public ExpenseClaimService(ExpenseClaimRepository expenseClaimRepository, UserService userService) {
        this.expenseClaimRepository = expenseClaimRepository;
        this.userService = userService;
    }

    public ExpenseDtos.ExpenseResponse createExpenseClaim(UserPrincipal principal, ExpenseDtos.CreateExpenseRequest request) {
        User user = userService.getUserEntityById(principal.getId());

        ExpenseClaim claim = ExpenseClaim.builder()
                .user(user)
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .status(RequestStatus.PENDING)
                .build();

        expenseClaimRepository.save(claim);
        log.info("User {} submitted expense claim {} for {}", user.getEmail(), claim.getId(), claim.getAmount());

        return toResponse(claim);
    }

    public List<ExpenseDtos.ExpenseResponse> getMyExpenseClaims(Long userId) {
        return expenseClaimRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public List<ExpenseDtos.ExpenseResponse> getPendingClaimsForManager(Long managerId) {
        return expenseClaimRepository.findByManagerIdAndStatus(managerId, RequestStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    public List<ExpenseDtos.ExpenseResponse> getAllClaimsForAdmin() {
        return expenseClaimRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ExpenseDtos.ExpenseResponse approveClaim(UserPrincipal principal, Long claimId, String comment) {
        return actOnClaim(principal, claimId, RequestStatus.APPROVED);
    }

    public ExpenseDtos.ExpenseResponse rejectClaim(UserPrincipal principal, Long claimId, String comment) {
        return actOnClaim(principal, claimId, RequestStatus.REJECTED);
    }

    private ExpenseDtos.ExpenseResponse actOnClaim(UserPrincipal principal, Long claimId, RequestStatus newStatus) {
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense claim not found with id: " + claimId));

        User actor = userService.getUserEntityById(principal.getId());

        boolean isDirectManager = claim.getUser().getManager() != null
                && claim.getUser().getManager().getId().equals(actor.getId());
        boolean isAdmin = actor.getRole() == Role.ADMIN;

        if (!isDirectManager && !isAdmin) {
            throw new UnauthorizedActionException("You are not authorized to act on this expense claim");
        }

        claim.setStatus(newStatus);
        expenseClaimRepository.save(claim);

        log.info("Expense claim {} {} by {}", claimId, newStatus, actor.getEmail());

        return toResponse(claim);
    }

    private ExpenseDtos.ExpenseResponse toResponse(ExpenseClaim claim) {
        return ExpenseDtos.ExpenseResponse.builder()
                .id(claim.getId())
                .userId(claim.getUser().getId())
                .userFullName(claim.getUser().getFullName())
                .category(claim.getCategory())
                .amount(claim.getAmount())
                .description(claim.getDescription())
                .expenseDate(claim.getExpenseDate())
                .status(claim.getStatus())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}