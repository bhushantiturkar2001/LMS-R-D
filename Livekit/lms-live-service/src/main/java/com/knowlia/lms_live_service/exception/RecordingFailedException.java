package com.knowlia.lms_live_service.exception;

/**
 * Thrown when a recording operation fails (start or stop).
 *
 * <p>IMPORTANT: Recording failure is non-critical — the live class
 * must continue even if recording fails. This exception should be
 * caught at the service level, logged, and NOT propagated to the
 * instructor or students.</p>
 *
 * <p>Maps to HTTP 500 Internal Server Error via {@code GlobalExceptionHandler}
 * only if it reaches the controller layer (which it should not in normal flow).</p>
 */
public class RecordingFailedException extends LmsException {

    private static final String ERROR_CODE = "RECORDING_FAILED";

    /**
     * @param roomName the room where recording failed
     * @param cause    the original exception from the Egress service
     */
    public RecordingFailedException(String roomName, Throwable cause) {
        super(ERROR_CODE,
            "Recording failed for room: " + roomName
            + ". The class will continue without recording.",
            cause);
    }

    /**
     * @param roomName the room where recording failed
     * @param reason   human-readable reason for the failure
     */
    public RecordingFailedException(String roomName, String reason) {
        super(ERROR_CODE,
            "Recording failed for room: " + roomName + ". Reason: " + reason);
    }
}
