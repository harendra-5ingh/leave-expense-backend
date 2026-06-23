package com.leaveexpense.leave_expense_backend.service;


import com.leaveexpense.leave_expense_backend.Repository.UserRepository;
import com.leaveexpense.leave_expense_backend.dto.UserDtos;
import com.leaveexpense.leave_expense_backend.entity.User;
import com.leaveexpense.leave_expense_backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserDtos.UserResponse getUserById(Long id) {
        return toResponse(getUserEntityById(id));
    }

    public List<UserDtos.UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<UserDtos.UserResponse> getTeamMembers(Long managerId) {
        return userRepository.findByManagerId(managerId).stream().map(this::toResponse).toList();
    }

    private UserDtos.UserResponse toResponse(User user) {
        return UserDtos.UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .managerName(user.getManager() != null ? user.getManager().getFullName() : null)
                .enabled(user.isEnabled())
                .build();
    }
}