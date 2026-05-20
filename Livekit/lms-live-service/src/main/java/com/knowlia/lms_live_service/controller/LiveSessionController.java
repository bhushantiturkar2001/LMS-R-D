package com.knowlia.lms_live_service.controller;

import com.knowlia.lms_live_service.dto.JoinRequest;
import com.knowlia.lms_live_service.dto.JoinResponse;
import com.knowlia.lms_live_service.dto.StartSessionRequest;
import com.knowlia.lms_live_service.service.LiveSessionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for live session management.
 * Handles starting, joining, ending, and checking status of live classes.
 */
@RestController
@RequestMapping("/api/v1/live")
public class LiveSessionController {

    private static final Logger log = LoggerFactory.getLogger(LiveSessionController.class);

    @Autowired
    private LiveSessionService liveSessionService;

    /**
     * Start a new live session (instructor only).
     * Creates a LiveKit room and saves session to database.
     *
     * @param request validated start session request
     * @return room name and instructor token
     */
    @PostMapping("/start")
    public ResponseEntity<JoinResponse> startSession(@Valid @RequestBody StartSessionRequest request) {
        log.info("Starting session for course: {}, instructor: {}", request.getCourseId(), request.getInstructorId());
        JoinResponse response = liveSessionService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Join an existing live session (student).
     * Validates room exists and generates student token.
     *
     * @param request validated join request
     * @return room name and student token
     */
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> joinSession(@Valid @RequestBody JoinRequest request) {
        log.info("Student {} joining room: {}", request.getStudentId(), request.getRoomName());
        JoinResponse response = liveSessionService.joinSession(request);
        return ResponseEntity.ok(response);
    }

    /**
     * End a live session (instructor only).
     * Closes the LiveKit room and marks session as ended.
     *
     * @param roomName the room to end
     * @return success message
     */
    @PostMapping("/end")
    public ResponseEntity<String> endSession(@RequestParam String roomName) {
        log.info("Ending session for room: {}", roomName);
        liveSessionService.endSession(roomName);
        return ResponseEntity.ok("Session ended successfully");
    }

    /**
     * Check if a room is currently active.
     *
     * @param roomName the room to check
     * @return true if room is active, false otherwise
     */
    @GetMapping("/status")
    public ResponseEntity<Boolean> isRoomActive(@RequestParam String roomName) {
        log.info("Checking status for room: {}", roomName);
        boolean isActive = liveSessionService.isRoomActive(roomName);
        return ResponseEntity.ok(isActive);
    }
}
