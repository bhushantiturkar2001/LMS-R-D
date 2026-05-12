package com.knowlia.lms_live_service.service;

import com.knowlia.lms_live_service.dto.JoinResponse;
import com.knowlia.lms_live_service.dto.StartSessionRequest;
import com.knowlia.lms_live_service.model.LiveSession;
import com.knowlia.lms_live_service.model.LiveSession.SessionStatus;
import com.knowlia.lms_live_service.repository.LiveSessionRepository;
import io.livekit.server.RoomServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LiveSessionService {

    @Autowired
    private LiveSessionRepository sessionRepository;

    @Autowired
    private LiveKitTokenService tokenService;

    @Autowired
    private RoomServiceClient roomServiceClient; // injected from LiveKitConfig bean

    @Value("${livekit.server.url}")
    private String liveKitServerUrl;

    /**
     * Called when instructor starts a class.
     * Creates room in LiveKit + saves session in DB.
     * Returns instructor token so they can connect immediately.
     */
    public JoinResponse startSession(StartSessionRequest req) throws Exception {

        // Unique room name — courseId + short UUID avoids collisions
        String roomName = req.getCourseId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        // Create room in LiveKit — emptyTimeout: auto-delete if empty for 5 min
        roomServiceClient.createRoom(
            roomName,
            300,   // emptyTimeout in seconds
            500    // maxParticipants
        ).execute();

        // Save session record in DB with ACTIVE status
        LiveSession session = new LiveSession();
        session.setRoomName(roomName);
        session.setCourseId(req.getCourseId());
        session.setInstructorId(req.getInstructorId());
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());
        sessionRepository.save(session);

        // Generate instructor token — canPublish: true, roomAdmin: true
        String token = tokenService.generateInstructorToken(
            roomName,
            req.getInstructorId(),
            req.getInstructorName()
        );

        return new JoinResponse(token, liveKitServerUrl);
    }

    /**
     * Called when student joins a class.
     * Verifies room is ACTIVE in DB, then returns student token.
     * Enrollment check is handled in controller before calling this.
     */
    public JoinResponse joinSession(String roomName, String studentId, String studentName) {

        // Room must be ACTIVE — if instructor hasn't started yet, throw error
        sessionRepository.findByRoomNameAndStatus(roomName, SessionStatus.ACTIVE)
            .orElseThrow(() -> new RuntimeException("Session not active or not found"));

        // Student token — canPublish: false (view only), canSubscribe: true
        String token = tokenService.generateStudentToken(roomName, studentId, studentName);

        return new JoinResponse(token, liveKitServerUrl);
    }

    /**
     * Called when instructor ends the class.
     * Deletes room from LiveKit — this automatically triggers "room_finished" webhook.
     * Then marks session ENDED in DB.
     */
    public void endSession(String roomName) throws Exception {

        // Deleting room from LiveKit fires webhook → attendance + recording processing
        roomServiceClient.deleteRoom(roomName).execute();

        // Mark session ENDED in DB
        LiveSession session = sessionRepository.findByRoomName(roomName)
            .orElseThrow(() -> new RuntimeException("Session not found: " + roomName));

        session.setStatus(SessionStatus.ENDED);
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);
    }

    /**
     * Quick boolean check — used by controller before generating student token.
     */
    public boolean isRoomActive(String roomName) {
        return sessionRepository.existsByRoomNameAndStatus(roomName, SessionStatus.ACTIVE);
    }
}
