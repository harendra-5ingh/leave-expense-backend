package com.leaveexpense.leave_expense_backend.Repository;



import com.leaveexpense.leave_expense_backend.entity.LeaveRequest;
import com.leaveexpense.leave_expense_backend.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserId(Long userId);

    // Pending requests for employees who report to a specific manager
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.manager.id = :managerId AND lr.status = :status")
    List<LeaveRequest> findByManagerIdAndStatus(@Param("managerId") Long managerId,
                                                @Param("status") RequestStatus status);

    List<LeaveRequest> findByStatus(RequestStatus status);

    // Requests that have been PENDING for longer than the cutoff — used for escalation logic
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' AND lr.createdAt < :cutoff")
    List<LeaveRequest> findStaleRequests(@Param("cutoff") LocalDateTime cutoff);
}