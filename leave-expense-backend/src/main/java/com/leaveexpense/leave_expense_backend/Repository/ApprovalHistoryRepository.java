package com.leaveexpense.leave_expense_backend.Repository;

import com.leaveexpense.leave_expense_backend.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
    List<ApprovalHistory> findByLeaveRequestId(Long leaveRequestId);
}