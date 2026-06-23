package com.leaveexpense.leave_expense_backend.exception;


public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}