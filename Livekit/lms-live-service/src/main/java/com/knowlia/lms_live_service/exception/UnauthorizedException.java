package com.knowlia.lms_live_service.exception;

/**
 * Thrown when a user attempts an action they are not permitted to perform.
 *
 * <p>Scenarios:
 * <ul>
 *   <li>Student tries to mute or kick another participant</li>
 *   <li>Non-instructor tries to end a class</li>
 *   <li>Service-to-service call missing the internal secret header</li>
 *   <li>Participant identity in token does not match request identity</li>
 * </ul>
 * </p>
 *
 * <p>Maps to HTTP 403 Forbidden via {@code GlobalExceptionHandler}.
 * Note: Use 403 (not 401) — the user is authenticated but not authorized.</p>
 */
public class UnauthorizedException extends LmsException {

    private static final String ERROR_CODE = "UNAUTHORIZED";

    /**
     * @param action     the action that was attempted (e.g. "mute participant")
     * @param identityId the identity of the user who attempted the action
     */
    public UnauthorizedException(String action, String identityId) {
        super(ERROR_CODE,
            "User " + identityId + " is not authorized to perform action: " + action);
    }

    /**
     * @param message custom message for specific unauthorized scenarios
     */
    public UnauthorizedException(String message) {
        super(ERROR_CODE, message);
    }
}
