package com.knowlia.lms_live_service.exception;

/**
 * Thrown when a student attempts to join a room that has reached
 * its maximum participant capacity.
 *
 * <p>Capacity is set at room creation time based on the institute's
 * plan and the number of enrolled students for that course.</p>
 *
 * <p>Maps to HTTP 409 Conflict via {@code GlobalExceptionHandler}.</p>
 */
public class RoomCapacityExceededException extends LmsException {

    private static final String ERROR_CODE = "ROOM_CAPACITY_EXCEEDED";

    /**
     * @param roomName    the room that is full
     * @param maxCapacity the maximum number of participants allowed
     */
    public RoomCapacityExceededException(String roomName, int maxCapacity) {
        super(ERROR_CODE,
            "Room " + roomName + " has reached its maximum capacity of "
            + maxCapacity + " participants. Please contact your instructor.");
    }
}
