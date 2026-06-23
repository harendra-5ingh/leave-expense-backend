package com.leaveexpense.leave_expense_backend.dto;


import com.leaveexpense.leave_expense_backend.entity.ExpenseCategory;
import com.leaveexpense.leave_expense_backend.entity.RequestStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateExpenseRequest {
        @NotNull(message = "Category is required")
        private ExpenseCategory category;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        private BigDecimal amount;

        private String description;

        private LocalDate expenseDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionRequest {
        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseResponse {
        private Long id;
        private Long userId;
        private String userFullName;
        private ExpenseCategory category;
        private BigDecimal amount;
        private String description;
        private LocalDate expenseDate;
        private RequestStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}