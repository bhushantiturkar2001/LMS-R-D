package com.knowlia.lms_live_service.dto;

import java.time.LocalDateTime;

/**
 * Standard error response returned to the client for all exceptions.
 *
 * <p>Every error from this service — validation failure, not found,
 * server error — returns this exact structure. This ensures the frontend
 * always knows what to expect and can handle errors consistently.</p>
 *
 * <p>Example response body:
 * <pre>
 * {
 *   "errorCode": "SESSION_NOT_FOUND",
 *   "message": "Live session not found for room: physics-101-abc123",
 *   "timestamp": "2026-05-19T13:45:00"
 * }
 * </pre>
 * </p>
 *
 * <p>IMPORTANT: Never include stack traces, internal class names,
 * or DB error details in this response — those go to server logs only.</p>
 */
public class ErrorResponse {

    /** Machine-readable code — frontend uses this for conditional logic. */
    private final String errorCode;

    /** Human-readable message — shown to user or logged by frontend. */
    private final String message;

    /** Server time when the error occurred — useful for debugging. */
    private final LocalDateTime timestamp;

    /**
     * @param errorCode machine-readable error code (e.g. "SESSION_NOT_FOUND")
     * @param message   human-readable description of what went wrong
     */
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
