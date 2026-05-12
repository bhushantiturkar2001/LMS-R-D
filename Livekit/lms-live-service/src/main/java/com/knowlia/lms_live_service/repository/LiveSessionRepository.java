package com.knowlia.lms_live_service.repository;

import com.knowlia.lms_live_service.model.LiveSession;
import com.knowlia.lms_live_service.model.LiveSession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {

    Optional<LiveSession> findByRoomName(String roomName);

    Optional<LiveSession> findByRoomNameAndStatus(String roomName, SessionStatus status);

    boolean existsByRoomNameAndStatus(String roomName, SessionStatus status);
}
