package com.leaveexpense.leave_expense_backend.controller;


import com.leaveexpense.leave_expense_backend.dto.LeaveDtos;
import com.leaveexpense.leave_expense_backend.security.UserPrincipal;
import com.leaveexpense.leave_expense_backend.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    public ResponseEntity<LeaveDtos.LeaveResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody LeaveDtos.CreateLeaveRequest request) {
        return ResponseEntity.ok(leaveRequestService.createLeaveRequest(principal, request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<LeaveDtos.LeaveResponse>> getMyRequests(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(leaveRequestService.getMyLeaveRequests(principal.getId()));
    }

    @GetMapping("/pending-for-team")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<LeaveDtos.LeaveResponse>> getPendingForTeam(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(leaveRequestService.getPendingRequestsForManager(principal.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveDtos.LeaveResponse>> getAll() {
        return ResponseEntity.ok(leaveRequestService.getAllRequestsForAdmin());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveDtos.LeaveResponse> approve(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable Long id,
                                                           @RequestBody(required = false) LeaveDtos.ActionRequest request) {
        String comment = request != null ? request.getComment() : null;
        return ResponseEntity.ok(leaveRequestService.approveRequest(principal, id, comment));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveDtos.LeaveResponse> reject(@AuthenticationPrincipal UserPrincipal principal,
                                                          @PathVariable Long id,
                                                          @RequestBody(required = false) LeaveDtos.ActionRequest request) {
        String comment = request != null ? request.getComment() : null;
        return ResponseEntity.ok(leaveRequestService.rejectRequest(principal, id, comment));
    }

    @PostMapping("/escalate-stale")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveDtos.LeaveResponse>> escalateStale(@RequestParam(defaultValue = "3") int days) {
        return ResponseEntity.ok(leaveRequestService.escalateStaleRequests(days));
    }
}