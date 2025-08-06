package io.github.Wanyui.springtodoapp.service.exception;

/**
 * Custom exception for service layer operations.
 * 
 * This exception is thrown when service operations fail due to
 * database errors, validation issues, or other business logic problems.
 * 
 * @author Wanyui/Jano
 * @version 1.0
 * @since 2025-08-06
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ServiceException with the specified message.
     * 
     * @param message the detail message
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new ServiceException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ServiceException with the specified cause.
     * 
     * @param cause the cause
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }
} 