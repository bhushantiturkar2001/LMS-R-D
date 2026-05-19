package com.knowlia.lms_live_service.exception;

/**
 * Thrown when a request contains invalid or missing data
 * that cannot be caught by {@code @Valid} bean validation alone.
 *
 * <p>Use this for business-level validation that requires
 * database or context checks, not just field-level annotation checks.
 * For annotation-based validation failures, Spring automatically throws
 * {@code MethodArgumentNotValidException} which is handled separately.</p>
 *
 * <p>Scenarios:
 * <ul>
 *   <li>roomName contains characters that are invalid for LiveKit</li>
 *   <li>startTime is in the past when scheduling a session</li>
 *   <li>studentId format is valid but contains suspicious patterns</li>
 * </ul>
 * </p>
 *
 * <p>Maps to HTTP 400 Bad Request via {@code GlobalExceptionHandler}.</p>
 */
public class InvalidRequestException extends LmsException {

    private static final String ERROR_CODE = "INVALID_REQUEST";

    /**
     * @param field   the field that failed validation (e.g. "roomName", "studentId")
     * @param reason  why the value is invalid
     */
    public InvalidRequestException(String field, String reason) {
        super(ERROR_CODE, "Invalid value for field '" + field + "': " + reason);
    }

    /**
     * @param message full custom message when field-level detail is not needed
     */
    public InvalidRequestException(String message) {
        super(ERROR_CODE, message);
    }
}
