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

@Service
public class LiveKitTokenService {

    @Value("${livekit.api.key}")
    private String apiKey;

    @Value("${livekit.api.secret}")
    private String apiSecret;

    // RoomAdmin(true) allows instructor to mute/kick participants
    public String generateInstructorToken(String roomName, String instructorId, String instructorName) {
        AccessToken token = new AccessToken(apiKey, apiSecret);
        token.setName(instructorName);
        token.setIdentity(instructorId);
        token.addGrants(
            new RoomJoin(true),
            new RoomName(roomName),
            new CanPublish(true),
            new CanSubscribe(true),
            new CanPublishData(true),
            new RoomAdmin(true)
        );
        token.setTtl(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS));
        return token.toJwt();
    }

    // CanPublish(false) — student is view-only by default
    // To allow Q&A unmute, update permissions via RoomServiceClient, not a new token
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
