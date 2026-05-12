package com.knowlia.lms_live_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "live_sessions")
public class LiveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;       // "physics-class-101"
    private String courseId;       // which course
    private String instructorId;   // who started

    @Enumerated(EnumType.STRING)
    private SessionStatus status;  // ACTIVE / ENDED

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String recordingUrl;   // S3 URL after recording done

    public enum SessionStatus {
        ACTIVE, ENDED
    }
}
