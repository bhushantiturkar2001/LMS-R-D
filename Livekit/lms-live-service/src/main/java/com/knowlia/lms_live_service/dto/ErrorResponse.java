package com.knowlia.lms_live_service.dto;

import java.time.LocalDateTime;

/**
 * Standard error response DTO for all API errors.
 * Returned by GlobalExceptionHandler to clients.
 * Never includes stack traces — only error code, message, and timestamp.
 */
public class ErrorResponse {

    private String errorCode;
    private String message;
    private LocalDateTime timestamp;

    /**
     * Constructor for error response.
     *
     * @param errorCode unique error identifier (e.g., SESSION_NOT_FOUND)
     * @param message user-friendly error message
     * @param timestamp when the error occurred
     */
    public ErrorResponse(String errorCode, String message, LocalDateTime timestamp) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
