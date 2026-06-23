package com.leaveexpense.leave_expense_backend.dto;



import com.leaveexpense.leave_expense_backend.entity.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateLeaveRequest {
        @NotNull(message = "Leave type is required")
        private LeaveType leaveType;

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date cannot be in the past")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionRequest {
        // comment is optional, e.g. reason for rejection
        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveResponse {
        private Long id;
        private Long userId;
        private String userFullName;
        private LeaveType leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private RequestStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}