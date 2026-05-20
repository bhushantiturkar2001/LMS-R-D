package com.knowlia.lms_live_service.exception;

/**
 * Exception thrown when a live session or room is not found.
 * HTTP Status: 404 Not Found
 *
 * <p>Fired when:
 * - Student tries to join a room that doesn't exist
 * - Instructor tries to end a session that doesn't exist
 * - Any operation references a non-existent room</p>
 */
public class SessionNotFoundException extends LmsException {

    private static final String ERROR_CODE = "SESSION_NOT_FOUND";

    /**
     * @param roomName the room that was not found
     */
    public SessionNotFoundException(String roomName) {
        super(ERROR_CODE, "Live session not found for room: " + roomName);
    }
}
