package com.knowlia.lms_live_service.webhook;

import com.knowlia.lms_live_service.model.LiveSession;
import com.knowlia.lms_live_service.model.LiveSession.SessionStatus;
import com.knowlia.lms_live_service.repository.LiveSessionRepository;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook.WebhookEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Receives and processes webhook events from LiveKit server.
 *
 * <p>LiveKit calls this endpoint automatically when room events occur
 * (room started, room finished, participant joined/left, recording done).
 * Each event is verified using HMAC signature before processing.</p>
 *
 * <p>Heavy processing (recording, notifications) should be sent to Kafka
 * from here — do not do heavy work synchronously in this controller,
 * as LiveKit expects a fast 200 OK response.</p>
 */
@RestController
@RequestMapping("/api/live/webhook")
public class LiveKitWebhookController {

    // Injected from application.yml
    @Value("${livekit.api.key}")
    private String apiKey;

    @Value("${livekit.webhook.secret}")
    private String webhookSecret;

    private final LiveSessionRepository sessionRepository;

    public LiveKitWebhookController(LiveSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Main webhook handler — receives all LiveKit room events.
     *
     * <p>Verifies the request signature first. If invalid, returns 401.
     * Processes event based on type — updates DB for critical events,
     * publishes to Kafka for async processing (when Kafka is added).</p>
     *
     * @param body       raw JSON body from LiveKit
     * @param authHeader Authorization header used to verify webhook signature
     * @return 200 OK always after processing — LiveKit retries on non-200
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        WebhookEvent event;

        try {
            // Verify signature — prevents fake webhook calls from outside
            WebhookReceiver receiver = new WebhookReceiver(apiKey, webhookSecret);
            event = receiver.receive(body, authHeader);
        } catch (Exception e) {
            // Invalid signature — reject the request
            return ResponseEntity.status(401).build();
        }

        // Route to correct handler based on event type
        switch (event.getEvent()) {

            case "room_started":
                // Room created in LiveKit — class officially started
                handleRoomStarted(event);
                break;

            case "room_finished":
                // Room deleted — class ended, all participants disconnected
                // TODO: publish to Kafka → Recording Service + Notification Service
                handleRoomFinished(event);
                break;

            case "participant_joined":
                // A participant connected to the room
                // TODO: publish to Kafka → Attendance Service
                handleParticipantJoined(event);
                break;

            case "participant_left":
                // A participant disconnected from the room
                // TODO: publish to Kafka → Attendance Service (calculate duration)
                handleParticipantLeft(event);
                break;

            case "egress_ended":
                // Recording file is ready — S3 URL is now available
                // TODO: publish to Kafka → save URL + notify students
                handleEgressEnded(event);
                break;

            default:
                // Unhandled event — log and ignore
                System.out.println("Unhandled webhook event: " + event.getEvent());
        }

        // Always return 200 quickly — LiveKit retries if it gets non-200
        return ResponseEntity.ok().build();
    }

    /**
     * Handles room_started event.
     * Room already exists in DB (created in startSession) — just log for now.
     */
    private void handleRoomStarted(WebhookEvent event) {
        String roomName = event.getRoom().getName();
        System.out.println("Room started: " + roomName);
        // DB update already done in LiveSessionService.startSession()
    }

    /**
     * Handles room_finished event.
     * Marks session ENDED in DB if not already done.
     * In full system: publish Kafka event for recording + notification processing.
     */
    private void handleRoomFinished(WebhookEvent event) {
        String roomName = event.getRoom().getName();

        Optional<LiveSession> session = sessionRepository.findByRoomName(roomName);
        session.ifPresent(s -> {
            // Guard: only update if not already ENDED (endSession() may have done it)
            if (s.getStatus() != SessionStatus.ENDED) {
                s.setStatus(SessionStatus.ENDED);
                s.setEndTime(LocalDateTime.now());
                sessionRepository.save(s);
            }
        });

        System.out.println("Room finished: " + roomName);
    }

    /**
     * Handles participant_joined event.
     * In full system: publish to Kafka → Attendance Service marks join time.
     */
    private void handleParticipantJoined(WebhookEvent event) {
        String roomName = event.getRoom().getName();
        String identity = event.getParticipant().getIdentity();
        System.out.println("Participant joined: " + identity + " in room: " + roomName);
        // TODO: kafkaTemplate.send("attendance-events", new AttendanceEvent(roomName, identity, "JOINED"))
    }

    /**
     * Handles participant_left event.
     * In full system: publish to Kafka → Attendance Service calculates duration.
     */
    private void handleParticipantLeft(WebhookEvent event) {
        String roomName = event.getRoom().getName();
        String identity = event.getParticipant().getIdentity();
        System.out.println("Participant left: " + identity + " in room: " + roomName);
        // TODO: kafkaTemplate.send("attendance-events", new AttendanceEvent(roomName, identity, "LEFT"))
    }

    /**
     * Handles egress_ended event — recording file is ready on S3.
     * Saves recording URL to session record.
     * In full system: publish to Kafka → notify students recording is available.
     */
    private void handleEgressEnded(WebhookEvent event) {
        String roomName = event.getEgressInfo().getRoomName();

        // Get S3 URL from egress result
        if (event.getEgressInfo().getFileResultsCount() > 0) {
            String recordingUrl = event.getEgressInfo().getFileResults(0).getLocation();

            sessionRepository.findByRoomName(roomName).ifPresent(s -> {
                s.setRecordingUrl(recordingUrl);
                sessionRepository.save(s);
            });

            System.out.println("Recording ready for room: " + roomName + " → " + recordingUrl);
            // TODO: kafkaTemplate.send("recording-events", new RecordingDoneEvent(roomName, recordingUrl))
        }
    }
}
