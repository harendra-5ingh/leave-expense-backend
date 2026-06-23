package com.leaveexpense.leave_expense_backend.Repository;



import com.leaveexpense.leave_expense_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // All employees who report directly to this manager
    List<User> findByManagerId(Long managerId);
}