package com.knowlia.lms_live_service.exception;

/**
 * Thrown when an instructor attempts to start a new class
 * while they already have an active session running.
 *
 * <p>Prevents an instructor from accidentally running two classes
 * simultaneously, which would split students and waste bandwidth.</p>
 *
 * <p>Maps to HTTP 409 Conflict via {@code GlobalExceptionHandler}.</p>
 */
public class SessionAlreadyActiveException extends LmsException {

    private static final String ERROR_CODE = "SESSION_ALREADY_ACTIVE";

    /**
     * @param instructorId the instructor who already has an active session
     * @param existingRoomName the room name of the already-active session
     */
    public SessionAlreadyActiveException(String instructorId, String existingRoomName) {
        super(ERROR_CODE,
            "Instructor " + instructorId + " already has an active class running in room: "
            + existingRoomName + ". Please end it before starting a new one.");
    }
}
