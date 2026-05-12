package com.knowlia.lms_live_service.dto;

import lombok.Data;

@Data
public class StartSessionRequest {
    private String courseId;
    private String instructorId;
    private String instructorName;
}
