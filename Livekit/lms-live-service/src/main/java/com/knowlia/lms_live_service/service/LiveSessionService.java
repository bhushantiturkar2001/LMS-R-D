package com.knowlia.lms_live_service.service;

import com.knowlia.lms_live_service.dto.JoinRequest;
import com.knowlia.lms_live_service.dto.JoinResponse;
import com.knowlia.lms_live_service.dto.StartSessionRequest;
import com.knowlia.lms_live_service.exception.LiveKitServerException;
import com.knowlia.lms_live_service.exception.SessionNotFoundException;
import com.knowlia.lms_live_service.model.LiveSession;
import com.knowlia.lms_live_service.model.LiveSession.SessionStatus;
import com.knowlia.lms_live_service.repository.LiveSessionRepository;
import io.livekit.server.RoomServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LiveSessionService {

    private static final Logger log = LoggerFactory.getLogger(LiveSessionService.class);

    @Autowired
    private LiveSessionRepository sessionRepository;

    @Autowired
    private LiveKitTokenService tokenService;

    @Autowired
    private RoomServiceClient roomServiceClient;

    @Value("${livekit.server.url}")
    private String liveKitServerUrl;

    public JoinResponse startSession(StartSessionRequest req) {

        String roomName = req.getCourseId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Starting session | instructorId={} | courseId={} | roomName={}",
                req.getInstructorId(), req.getCourseId(), roomName);

        try {
            roomServiceClient.createRoom(roomName, 300, 500).execute();
        } catch (Exception e) {
            log.error("Failed to create LiveKit room | roomName={} | error={}", roomName, e.getMessage());
            throw new LiveKitServerException("create room", e);
        }

        LiveSession session = new LiveSession();
        session.setRoomName(roomName);
        session.setCourseId(req.getCourseId());
        session.setInstructorId(req.getInstructorId());
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());

        try {
            sessionRepository.save(session);
        } catch (Exception e) {
            // Rollback — room created in LiveKit but DB failed
            log.error("DB save failed — rolling back LiveKit room | roomName={}", roomName);
            tryDeleteRoom(roomName);
            throw new LiveKitServerException("save session after room creation", e);
        }

        String token = tokenService.generateInstructorToken(roomName, req.getInstructorId(), req.getInstructorName());
        log.info("Session started | roomName={}", roomName);
        return new JoinResponse(token, liveKitServerUrl, roomName);
    }

    public JoinResponse joinSession(JoinRequest req) {

        log.info("Student joining | roomName={} | studentId={}", req.getRoomName(), req.getStudentId());

        sessionRepository.findByRoomNameAndStatus(req.getRoomName(), SessionStatus.ACTIVE)
            .orElseThrow(() -> {
                log.warn("Join attempt for inactive session | roomName={} | studentId={}", req.getRoomName(), req.getStudentId());
                return new SessionNotFoundException(req.getRoomName());
            });

        String token = tokenService.generateStudentToken(req.getRoomName(), req.getStudentId(), req.getStudentName());
        return new JoinResponse(token, liveKitServerUrl, req.getRoomName());
    }

    public void endSession(String roomName) {

        log.info("Ending session | roomName={}", roomName);

        try {
            roomServiceClient.deleteRoom(roomName).execute();
        } catch (Exception e) {
            // Log but continue — session must be marked ENDED regardless
            log.error("Failed to delete LiveKit room | roomName={} | error={}", roomName, e.getMessage());
        }

        LiveSession session = sessionRepository.findByRoomName(roomName)
            .orElseThrow(() -> new SessionNotFoundException(roomName));

        session.setStatus(SessionStatus.ENDED);
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);
        log.info("Session ended | roomName={}", roomName);
    }

    public boolean isRoomActive(String roomName) {
        return sessionRepository.existsByRoomNameAndStatus(roomName, SessionStatus.ACTIVE);
    }

    private void tryDeleteRoom(String roomName) {
        try {
            roomServiceClient.deleteRoom(roomName).execute();
            log.info("Rollback successful | roomName={}", roomName);
        } catch (Exception e) {
            log.error("Rollback failed — orphaned LiveKit room | roomName={} | error={}", roomName, e.getMessage());
        }
    }
}
