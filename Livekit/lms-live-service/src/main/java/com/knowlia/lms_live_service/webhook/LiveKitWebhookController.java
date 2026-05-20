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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/live/webhook")
public class LiveKitWebhookController {

    private static final Logger log = LoggerFactory.getLogger(LiveKitWebhookController.class);

    @Value("${livekit.api.key}")
    private String apiKey;

    @Value("${livekit.webhook.secret}")
    private String webhookSecret;

    // 1.5.3 Webhook Security — IP Whitelist
    @Value("${livekit.webhook.allowed-ips:127.0.0.1,localhost}")
    private String allowedIpsString;

    private final LiveSessionRepository sessionRepository;

    public LiveKitWebhookController(LiveSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // LiveKit retries on non-200 — always return 200 quickly
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String body,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "X-Real-IP", required = false) String realIp,
            @RequestHeader(value = "Host", required = false) String host) {

        // 1.5.3 Webhook Security — IP Whitelist check
        String clientIp = extractClientIp(forwardedFor, realIp, host);
        if (!isIpAllowed(clientIp)) {
            log.warn("Webhook rejected — unauthorized IP | ip={}", clientIp);
            return ResponseEntity.status(403).build();
        }

        WebhookEvent event;
        try {
            WebhookReceiver receiver = new WebhookReceiver(apiKey, webhookSecret);
            event = receiver.receive(body, authHeader);
            log.info("Webhook received | event={} | roomName={} | ip={}",
                    event.getEvent(), event.hasRoom() ? event.getRoom().getName() : "N/A", clientIp);
        } catch (Exception e) {
            log.warn("Webhook signature verification failed | error={} | ip={}", e.getMessage(), clientIp);
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

    /**
     * Extract client IP from various headers (handles proxies/load balancers).
     */
    private String extractClientIp(String forwardedFor, String realIp, String host) {
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // X-Forwarded-For may contain multiple IPs — take the first one
            return forwardedFor.split(",")[0].trim();
        }
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        if (host != null && !host.isEmpty()) {
            // Host header may contain port — extract just the IP/hostname
            return host.split(":")[0];
        }
        return "unknown";
    }

    /**
     * Check if the client IP is in the whitelist.
     */
    private boolean isIpAllowed(String clientIp) {
        if (clientIp == null || clientIp.equals("unknown")) {
            log.warn("Could not determine client IP for webhook");
            return false;
        }

        Set<String> allowedIps = Arrays.stream(allowedIpsString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        String normalizedIp = clientIp.toLowerCase();

        // Also check if it's localhost variations
        if (normalizedIp.equals("127.0.0.1") || normalizedIp.equals("localhost")) {
            return allowedIps.contains("127.0.0.1") || allowedIps.contains("localhost");
        }

        return allowedIps.contains(normalizedIp);
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

        // 1.5 Idempotency — skip if already ENDED (endSession() may have done it)
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
            // 1.5 Idempotency — skip if already saved
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
