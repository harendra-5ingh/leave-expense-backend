package com.leaveexpense.leave_expense_backend.service;


import com.leaveexpense.leave_expense_backend.Repository.UserRepository;
import com.leaveexpense.leave_expense_backend.dto.AuthDtos;
import com.leaveexpense.leave_expense_backend.entity.Role;
import com.leaveexpense.leave_expense_backend.entity.User;
import com.leaveexpense.leave_expense_backend.exception.DuplicateResourceException;
import com.leaveexpense.leave_expense_backend.exception.ResourceNotFoundException;
import com.leaveexpense.leave_expense_backend.security.JwtService;
import com.leaveexpense.leave_expense_backend.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
        }

        Role role = Role.EMPLOYEE;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            role = Role.valueOf(request.getRole().toUpperCase());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .department(request.getDepartment())
                .manager(manager)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("Registered new user: {} with role {}", user.getEmail(), user.getRole());

        String token = jwtService.generateToken(new UserPrincipal(user));

        return new AuthDtos.AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(new UserPrincipal(user));
        log.info("User logged in: {}", user.getEmail());

        return new AuthDtos.AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }
}