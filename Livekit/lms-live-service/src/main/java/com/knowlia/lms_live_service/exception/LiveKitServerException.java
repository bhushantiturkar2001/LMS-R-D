package com.knowlia.lms_live_service.exception;

/**
 * Thrown when communication with the LiveKit server fails.
 *
 * <p>Scenarios:
 * <ul>
 *   <li>LiveKit server is unreachable (Docker not running)</li>
 *   <li>Room creation fails on LiveKit side</li>
 *   <li>Room deletion fails on LiveKit side</li>
 *   <li>Token generation fails due to invalid API key/secret</li>
 * </ul>
 * </p>
 *
 * <p>Maps to HTTP 503 Service Unavailable via {@code GlobalExceptionHandler}.
 * The class continues even if LiveKit is down — this exception is thrown
 * only for operations that cannot proceed without LiveKit.</p>
 */
public class LiveKitServerException extends LmsException {

    private static final String ERROR_CODE = "LIVEKIT_SERVER_ERROR";

    /**
     * @param operation the operation that failed (e.g. "create room", "delete room")
     * @param cause     the original exception from the LiveKit SDK
     */
    public LiveKitServerException(String operation, Throwable cause) {
        super(ERROR_CODE,
            "LiveKit server error during operation: " + operation
            + ". Please try again in a moment.",
            cause);
    }

    /**
     * @param message custom message when cause is not available
     */
    public LiveKitServerException(String message) {
        super(ERROR_CODE, message);
    }
}
