package com.leaveexpense.leave_expense_backend.dto;


import com.leaveexpense.leave_expense_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private Role role;
        private String department;
        private Long managerId;
        private String managerName;
        private boolean enabled;
    }
}