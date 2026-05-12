package com.knowlia.lms_live_service.service;

import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanPublishData;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomAdmin;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Generates signed JWT tokens for LiveKit room access.
 * Tokens are created server-side to prevent clients from
 * forging their own permissions (e.g. a student giving
 * themselves publish rights).
 */
@Service
public class LiveKitTokenService {

    // Injected from application.yml 
    @Value("${livekit.api.key}")
    private String apiKey;

    @Value("${livekit.api.secret}")
    private String apiSecret;

    /**
     * Instructor token — full control over the room.
     * CanPublish(true)  → can share camera, mic, screen
     * RoomAdmin(true)   → can mute or kick participants
     * TTL 4 hours       → longer session for instructor
     */
    public String generateInstructorToken(String roomName, String instructorId, String instructorName) {
        AccessToken token = new AccessToken(apiKey, apiSecret);
        token.setName(instructorName);
        token.setIdentity(instructorId); // identity must be unique per participant in a room

        // Each permission is a separate class — VideoGrant is a sealed class, not instantiable directly
        token.addGrants(
            new RoomJoin(true),
            new RoomName(roomName),
            new CanPublish(true),
            new CanSubscribe(true),
            new CanPublishData(true),    // needed for chat via LiveKit data channel
            new RoomAdmin(true)          // allows mute/kick from instructor dashboard
        );

        token.setTtl(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS));
        return token.toJwt();
    }

    /**
     * Student token — view only by default.
     * CanPublish(false)     → student cannot broadcast video/audio
     * CanPublishData(true)  → still allows chat messages
     * TTL 2 hours           → shorter, students rarely stay full session
     *
     * Note: if you want to allow student to unmute for Q&A,
     * update participant permissions via RoomServiceClient,
     * not by issuing a new token.
     */
    public String generateStudentToken(String roomName, String studentId, String studentName) {
        AccessToken token = new AccessToken(apiKey, apiSecret);
        token.setName(studentName);
        token.setIdentity(studentId);

        token.addGrants(
            new RoomJoin(true),
            new RoomName(roomName),
            new CanPublish(false),
            new CanSubscribe(true),
            new CanPublishData(true)
        );

        token.setTtl(TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS));
        return token.toJwt();
    }
}
