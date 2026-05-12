package com.knowlia.lms_live_service.dto;

import lombok.Data;

@Data
public class JoinRequest {
    private String roomName;
    private String courseId;
    private String studentId;
    private String studentName;
}
