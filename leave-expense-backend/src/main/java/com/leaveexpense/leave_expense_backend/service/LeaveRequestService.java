package com.leaveexpense.leave_expense_backend.service;


import com.leaveexpense.leave_expense_backend.Repository.ApprovalHistoryRepository;
import com.leaveexpense.leave_expense_backend.Repository.LeaveRequestRepository;
import com.leaveexpense.leave_expense_backend.dto.LeaveDtos;
import com.leaveexpense.leave_expense_backend.entity.*;
import com.leaveexpense.leave_expense_backend.exception.ResourceNotFoundException;
import com.leaveexpense.leave_expense_backend.exception.UnauthorizedActionException;
import com.leaveexpense.leave_expense_backend.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final UserService userService;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository,
                               ApprovalHistoryRepository approvalHistoryRepository,
                               UserService userService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.userService = userService;
    }

    public LeaveDtos.LeaveResponse createLeaveRequest(UserPrincipal principal, LeaveDtos.CreateLeaveRequest request) {
        User user = userService.getUserEntityById(principal.getId());

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .user(user)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .build();

        leaveRequestRepository.save(leaveRequest);
        log.info("User {} created leave request {}", user.getEmail(), leaveRequest.getId());

        return toResponse(leaveRequest);
    }

    public List<LeaveDtos.LeaveResponse> getMyLeaveRequests(Long userId) {
        return leaveRequestRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public List<LeaveDtos.LeaveResponse> getPendingRequestsForManager(Long managerId) {
        return leaveRequestRepository.findByManagerIdAndStatus(managerId, RequestStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    public List<LeaveDtos.LeaveResponse> getAllRequestsForAdmin() {
        return leaveRequestRepository.findAll().stream().map(this::toResponse).toList();
    }

    public LeaveDtos.LeaveResponse approveRequest(UserPrincipal principal, Long requestId, String comment) {
        return actOnRequest(principal, requestId, RequestStatus.APPROVED, ApprovalAction.APPROVED, comment);
    }

    public LeaveDtos.LeaveResponse rejectRequest(UserPrincipal principal, Long requestId, String comment) {
        return actOnRequest(principal, requestId, RequestStatus.REJECTED, ApprovalAction.REJECTED, comment);
    }

    private LeaveDtos.LeaveResponse actOnRequest(UserPrincipal principal, Long requestId,
                                                 RequestStatus newStatus, ApprovalAction action, String comment) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + requestId));

        User actor = userService.getUserEntityById(principal.getId());

        // Authorization check: only the employee's direct manager or an admin can act
        boolean isDirectManager = leaveRequest.getUser().getManager() != null
                && leaveRequest.getUser().getManager().getId().equals(actor.getId());
        boolean isAdmin = actor.getRole() == Role.ADMIN;

        if (!isDirectManager && !isAdmin) {
            throw new UnauthorizedActionException("You are not authorized to act on this leave request");
        }

        leaveRequest.setStatus(newStatus);
        leaveRequestRepository.save(leaveRequest);

        ApprovalHistory history = ApprovalHistory.builder()
                .leaveRequest(leaveRequest)
                .actedBy(actor)
                .action(action)
                .comment(comment)
                .build();
        approvalHistoryRepository.save(history);

        log.info("Leave request {} {} by {}", requestId, newStatus, actor.getEmail());

        return toResponse(leaveRequest);
    }

    /**
     * Escalates leave requests that have been pending for more than the given
     * number of days. Intended to be called by a scheduled job or an admin-triggered
     * endpoint. Demonstrates time-based business logic beyond simple CRUD.
     */
    public List<LeaveDtos.LeaveResponse> escalateStaleRequests(int daysThreshold) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysThreshold);
        List<LeaveRequest> staleRequests = leaveRequestRepository.findStaleRequests(cutoff);

        staleRequests.forEach(lr -> lr.setStatus(RequestStatus.ESCALATED));
        leaveRequestRepository.saveAll(staleRequests);

        log.info("Escalated {} stale leave requests older than {} days", staleRequests.size(), daysThreshold);

        return staleRequests.stream().map(this::toResponse).toList();
    }

    private LeaveDtos.LeaveResponse toResponse(LeaveRequest lr) {
        return LeaveDtos.LeaveResponse.builder()
                .id(lr.getId())
                .userId(lr.getUser().getId())
                .userFullName(lr.getUser().getFullName())
                .leaveType(lr.getLeaveType())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .createdAt(lr.getCreatedAt())
                .updatedAt(lr.getUpdatedAt())
                .build();
    }
}