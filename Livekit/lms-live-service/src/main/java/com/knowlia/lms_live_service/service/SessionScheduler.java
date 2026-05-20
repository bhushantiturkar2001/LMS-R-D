package com.knowlia.lms_live_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for session management.
 *
 * <p>Runs periodic checks to:
 * - Auto-end sessions that exceed their scheduled duration
 * - Clean up any orphaned sessions</p>
 */
@Component
public class SessionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SessionScheduler.class);

    @Autowired
    private LiveSessionService liveSessionService;

    /**
     * Check every minute for sessions that have exceeded their scheduled duration.
     * Cron: "0 * * * * *" = every minute at second 0
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkAndAutoEndExpiredSessions() {
        try {
            liveSessionService.autoEndExpiredSessions();
        } catch (Exception e) {
            log.error("Error in auto-end scheduler task", e);
        }
    }
}
