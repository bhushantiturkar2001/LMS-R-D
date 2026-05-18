package com.knowlia.lms_live_service.controller;

import com.knowlia.lms_live_service.dto.JoinRequest;
import com.knowlia.lms_live_service.dto.JoinResponse;
import com.knowlia.lms_live_service.dto.StartSessionRequest;
import com.knowlia.lms_live_service.service.LiveSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing live class sessions.
 *
 * <p>Handles three operations: start a session (instructor),
 * join a session (student), and end a session (instructor).
 * Token generation and LiveKit communication is delegated to
 * {@link LiveSessionService} — controller only handles HTTP layer.</p>
 */
@RestController
@RequestMapping("/api/live")
public class LiveSessionController {

    @Autowired
    private LiveSessionService liveSessionService;

    /**
     * Instructor starts a live class.
     * Creates a room in LiveKit and saves session in DB.
     *
     * @param req contains courseId, instructorId, instructorName
     * @return JoinResponse with instructor token and LiveKit server URL
     */
    @PostMapping("/start")
    public ResponseEntity<JoinResponse> startSession(@RequestBody StartSessionRequest req) {
        try {
            JoinResponse response = liveSessionService.startSession(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return 500 with error message — LiveKit server may be unreachable
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Student joins an active live class.
     * Validates room is ACTIVE before issuing token.
     *
     * <p>Returns 404 if instructor has not started the class yet.
     * Enrollment check is intentionally skipped here for now —
     * add it when Student Service is available.</p>
     *
     * @param req contains roomName, studentId, studentName, courseId
     * @return JoinResponse with student token and LiveKit server URL
     */
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> joinSession(@RequestBody JoinRequest req) {
        try {
            JoinResponse response = liveSessionService.joinSession(
                req.getRoomName(),
                req.getStudentId(),
                req.getStudentName()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Session not active or not found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Instructor ends the live class.
     * Deletes room from LiveKit — triggers room_finished webhook automatically.
     *
     * @param roomName the LiveKit room identifier to end
     * @return 200 OK on success, 500 if LiveKit call fails
     */
    @PostMapping("/end")
    public ResponseEntity<Void> endSession(@RequestParam String roomName) {
        try {
            liveSessionService.endSession(roomName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check — verify if a room is currently active.
     * Used by frontend to show "Class is Live" indicator.
     *
     * @param roomName the room to check
     * @return 200 if active, 404 if not active
     */
    @GetMapping("/status")
    public ResponseEntity<Void> checkRoomStatus(@RequestParam String roomName) {
        boolean active = liveSessionService.isRoomActive(roomName);
        return active ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
 