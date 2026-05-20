package com.knowlia.lms_live_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "live_sessions", indexes = {
        @Index(name = "idx_room_name", columnList = "roomName"),
        @Index(name = "idx_instructor_status", columnList = "instructorId, status"),
        @Index(name = "idx_course_id", columnList = "courseId")
})
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
    private LocalDateTime scheduledEndTime;  // Auto-end timestamp
    private String recordingUrl;   // S3 URL after recording done

    public enum SessionStatus {
        ACTIVE, ENDED
    }
}
