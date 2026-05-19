package com.knowlia.lms_live_service.exception;

/**
 * Base exception for all LMS live service exceptions.
 *
 * <p>All custom exceptions in this service extend this class.
 * This allows catching all LMS-specific exceptions in one place
 * when needed, while still allowing specific handling per type.</p>
 *
 * <p>Usage: Never throw this directly — always throw a specific subclass.</p>
 */
public class LmsException extends RuntimeException {

    /**
     * Machine-readable error code returned to the client.
     * Example: "SESSION_NOT_FOUND", "LIVEKIT_SERVER_ERROR"
     */
    private final String errorCode;

    /**
     * @param errorCode machine-readable code for the client (e.g. "SESSION_NOT_FOUND")
     * @param message   human-readable message for logs and client response
     */
    public LmsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * @param errorCode machine-readable code for the client
     * @param message   human-readable message
     * @param cause     original exception that caused this (for logging chain)
     */
    public LmsException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the machine-readable error code.
     * Used by {@code GlobalExceptionHandler} to build the error response.
     *
     * @return error code string
     */
    public String getErrorCode() {
        return errorCode;
    }
}
