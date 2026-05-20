package com.knowlia.lms_live_service.webhook;

import com.knowlia.lms_live_service.model.LiveSession;
import com.knowlia.lms_live_service.model.LiveSession.SessionStatus;
import com.knowlia.lms_live_service.repository.LiveSessionRepository;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook.WebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/live/webhook")
public class LiveKitWebhookController {

    private static final Logger log = LoggerFactory.getLogger(LiveKitWebhookController.class);

    @Value("${livekit.api.key}")
    private String apiKey;

    @Value("${livekit.webhook.secret}")
    private String webhookSecret;

    private final LiveSessionRepository sessionRepository;

    public LiveKitWebhookController(LiveSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // LiveKit retries on non-200 — always return 200 quickly
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        WebhookEvent event;
        try {
            WebhookReceiver receiver = new WebhookReceiver(apiKey, webhookSecret);
            event = receiver.receive(body, authHeader);
            log.info("Webhook received | event={} | roomName={}",
                    event.getEvent(), event.hasRoom() ? event.getRoom().getName() : "N/A");
        } catch (Exception e) {
            log.warn("Webhook signature verification failed | error={}", e.getMessage());
            return ResponseEntity.status(401).build();
        }

        switch (event.getEvent()) {
            case "room_started":    handleRoomStarted(event);    break;
            case "room_finished":   handleRoomFinished(event);   break;
            case "participant_joined": handleParticipantJoined(event); break;
            case "participant_left":   handleParticipantLeft(event);   break;
            case "egress_ended":    handleEgressEnded(event);    break;
            default:
                log.info("Unhandled webhook event | event={}", event.getEvent());
        }

        return ResponseEntity.ok().build();
    }

    private void handleRoomStarted(WebhookEvent event) {
        log.info("Room started | roomName={}", event.getRoom().getName());
    }

    private void handleRoomFinished(WebhookEvent event) {
        String roomName = event.getRoom().getName();
        Optional<LiveSession> sessionOpt = sessionRepository.findByRoomName(roomName);

        if (sessionOpt.isEmpty()) {
            log.warn("room_finished for unknown room | roomName={}", roomName);
            return;
        }

        LiveSession session = sessionOpt.get();

        // Idempotency — skip if already ENDED (endSession() may have done it)
        if (session.getStatus() == SessionStatus.ENDED) {
            log.warn("Duplicate room_finished webhook | roomName={}", roomName);
            return;
        }

        session.setStatus(SessionStatus.ENDED);
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);
        log.info("Session marked ENDED via webhook | roomName={}", roomName);
        // TODO Phase 2: WebClient → lms-recording-service to stop recording
    }

    private void handleParticipantJoined(WebhookEvent event) {
        log.info("Participant joined | roomName={} | identity={}",
                event.getRoom().getName(), event.getParticipant().getIdentity());
        // TODO Phase 4: Save attendance record with joinTime
    }

    private void handleParticipantLeft(WebhookEvent event) {
        log.info("Participant left | roomName={} | identity={}",
                event.getRoom().getName(), event.getParticipant().getIdentity());
        // TODO Phase 4: Update attendance with leaveTime + calculate duration
    }

    private void handleEgressEnded(WebhookEvent event) {
        String roomName = event.getEgressInfo().getRoomName();

        if (event.getEgressInfo().getFileResultsCount() == 0) {
            log.warn("egress_ended has no file results | roomName={}", roomName);
            return;
        }

        String recordingUrl = event.getEgressInfo().getFileResults(0).getLocation();

        sessionRepository.findByRoomName(roomName).ifPresent(session -> {
            // Idempotency — skip if already saved
            if (session.getRecordingUrl() != null) {
                log.warn("Duplicate egress_ended webhook | roomName={}", roomName);
                return;
            }
            session.setRecordingUrl(recordingUrl);
            sessionRepository.save(session);
            log.info("Recording URL saved | roomName={} | url={}", roomName, recordingUrl);
        });
        // TODO Phase 2: WebClient → lms-recording-service with recordingUrl
    }
}
