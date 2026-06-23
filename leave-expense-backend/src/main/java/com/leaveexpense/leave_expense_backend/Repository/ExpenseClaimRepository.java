package com.leaveexpense.leave_expense_backend.Repository;


import com.leaveexpense.leave_expense_backend.entity.ExpenseClaim;
import com.leaveexpense.leave_expense_backend.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Long> {

    List<ExpenseClaim> findByUserId(Long userId);

    @Query("SELECT ec FROM ExpenseClaim ec WHERE ec.user.manager.id = :managerId AND ec.status = :status")
    List<ExpenseClaim> findByManagerIdAndStatus(@Param("managerId") Long managerId,
                                                @Param("status") RequestStatus status);

    List<ExpenseClaim> findByStatus(RequestStatus status);
}