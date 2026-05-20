package com.knowlia.lms_live_service.exception;

import com.knowlia.lms_live_service.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 *
 * <p>Catches all exceptions thrown by controllers and services,
 * converts them to appropriate HTTP status codes, and returns
 * structured ErrorResponse DTOs to clients.</p>
 *
 * <p>Production Rule: Never expose stack traces to clients.
 * Always return user-friendly error messages with error codes.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles SessionNotFoundException (404 Not Found).
     * Fired when a live session or room is not found.
     *
     * @param ex the exception
     * @return 404 response with error details
     */
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFoundException(SessionNotFoundException ex) {
        log.warn("Session not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles SessionAlreadyActiveException (409 Conflict).
     * Fired when instructor tries to start a class while already teaching one.
     *
     * @param ex the exception
     * @return 409 response with error details
     */
    @ExceptionHandler(SessionAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handleSessionAlreadyActiveException(SessionAlreadyActiveException ex) {
        log.warn("Session already active: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles RoomCapacityExceededException (409 Conflict).
     * Fired when room is full and no more students can join.
     *
     * @param ex the exception
     * @return 409 response with error details
     */
    @ExceptionHandler(RoomCapacityExceededException.class)
    public ResponseEntity<ErrorResponse> handleRoomCapacityExceededException(RoomCapacityExceededException ex) {
        log.warn("Room capacity exceeded: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles LiveKitServerException (503 Service Unavailable).
     * Fired when LiveKit server is down or unreachable.
     *
     * @param ex the exception
     * @return 503 response with error details
     */
    @ExceptionHandler(LiveKitServerException.class)
    public ResponseEntity<ErrorResponse> handleLiveKitServerException(LiveKitServerException ex) {
        log.error("LiveKit server error: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handles RecordingFailedException (500 Internal Server Error).
     * Fired when recording fails but class can continue.
     *
     * @param ex the exception
     * @return 500 response with error details
     */
    @ExceptionHandler(RecordingFailedException.class)
    public ResponseEntity<ErrorResponse> handleRecordingFailedException(RecordingFailedException ex) {
        log.error("Recording failed: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handles UnauthorizedException (403 Forbidden).
     * Fired when user lacks permission to perform an action.
     *
     * @param ex the exception
     * @return 403 response with error details
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handles InvalidRequestException (400 Bad Request).
     * Fired when request parameters are invalid or missing.
     *
     * @param ex the exception
     * @return 400 response with error details
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles MethodArgumentNotValidException (400 Bad Request).
     * Fired when @Valid validation fails on request body.
     * Collects all field-level validation errors and returns them.
     *
     * @param ex the exception
     * @return 400 response with field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", fieldErrors);
        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed: " + fieldErrors,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles all other uncaught exceptions (500 Internal Server Error).
     * This is the fallback handler — catches anything not handled above.
     *
     * <p>Production Rule: Never expose the actual exception message or stack trace.
     * Always return a generic message to the client.</p>
     *
     * @param ex the exception
     * @return 500 response with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
