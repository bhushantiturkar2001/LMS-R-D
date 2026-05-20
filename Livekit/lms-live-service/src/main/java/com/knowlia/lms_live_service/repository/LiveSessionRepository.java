package com.knowlia.lms_live_service.repository;

import com.knowlia.lms_live_service.model.LiveSession;
import com.knowlia.lms_live_service.model.LiveSession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {

	Optional<LiveSession> findByRoomName(String roomName);

	Optional<LiveSession> findByRoomNameAndStatus(String roomName, SessionStatus status);

	boolean existsByRoomNameAndStatus(String roomName, SessionStatus status);

	/**
	 * Find active session for instructor — used to prevent concurrent sessions.
	 */
	Optional<LiveSession> findByInstructorIdAndStatus(String instructorId, SessionStatus status);

	/**
	 * Find sessions that have exceeded their scheduled end time and are still
	 * active. Used by scheduled task for auto-ending sessions.
	 */
	List<LiveSession> findByStatusAndScheduledEndTimeBefore(SessionStatus status, LocalDateTime time);
}
