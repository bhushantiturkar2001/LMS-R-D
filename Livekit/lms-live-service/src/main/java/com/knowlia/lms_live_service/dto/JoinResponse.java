package com.knowlia.lms_live_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinResponse {
    private String token;
    private String serverUrl;
}
