# LMS LiveKit — Build Tracker & Production Checklist
**Project:** lms-live-service + lms-recording-service + lms-schedule-service + LiveKit-Frontend
**Goal:** Production / Company Level Code
**Last Updated:** May 19, 2026

---

## Microservices Overview

| Service | Port | Status | Responsibility |
|---------|------|--------|---------------|
| lms-live-service | 8084 | Half Built | LiveKit room management, token generation, webhooks |
| lms-recording-service | 8085 | Not Started | Egress recording, file storage, recording list |
| lms-schedule-service | 8086 | Not Started | Weekly schedule, slot booking, bandwidth reservation |
| LiveKit-Frontend | 3000 | Half Built | React UI for instructor + student |
| LiveKit Server | 7880 | Docker Ready | SFU video routing |
| LiveKit Egress | - | Not Started | Recording Docker container |

---

## How to Use This File
- [x] = Done
- [ ] = Not done yet
- Update this file after every completed task

---

## CORRECT BUILD SEQUENCE

Phase 1   -> Foundation in lms-live-service           (1 day)
Phase 1.5 -> Critical Security (moved from Phase 7)   (half day)
Phase 2   -> lms-recording-service (new microservice) (2 days)
Phase 3   -> Core Features in lms-live-service + UI   (2 days)
Phase 4   -> UX and Polish                            (2 days)
Phase 5   -> Production Hardening                     (1 day)
Phase 6   -> lms-schedule-service (new microservice)  (3 days)
Phase 7   -> Remaining Security and Hardening         (1 day)
Phase 8   -> Edge Cases and Reliability               (1 day)

---

## WHAT IS ALREADY BUILT

### Backend - lms-live-service

- [x] Spring Boot project setup (port 8084)
- [x] H2 in-memory database configured
- [x] LiveKit Java SDK integrated (v0.12.1)
- [x] LiveKitConfig.java - RoomServiceClient bean
- [x] CorsConfig.java - CORS configured
- [x] LiveSession.java - Entity (roomName, courseId, instructorId, status, startTime, endTime, recordingUrl)
- [x] LiveSessionRepository.java - JPA repository
- [x] LiveKitTokenService.java - Instructor token + Student token
- [x] LiveSessionService.java - startSession, joinSession, endSession, isRoomActive
- [x] LiveSessionController.java - POST /start, POST /join, POST /end, GET /status
- [x] LiveKitWebhookController.java - room_started, room_finished, participant_joined, participant_left, egress_ended
- [x] StartSessionRequest.java DTO
- [x] JoinRequest.java DTO
- [x] JoinResponse.java DTO
- [x] application.yml - LiveKit keys, H2 config, server port

### Frontend - LiveKit-Frontend

- [x] React + Vite project setup
- [x] LiveKit React SDK integrated
- [x] RoleSelector.jsx - Choose Instructor or Student
- [x] InstructorLobby.jsx - Start class form
- [x] StudentLobby.jsx - Join class form
- [x] VideoConference.jsx - Grid layout, ControlBar, Audio renderer, Leave button
- [x] App.jsx - Screen navigation (role -> lobby -> room)
- [x] liveApi.js - startSession, joinSession, endSession API calls


---

## PHASE 1 - Foundation (lms-live-service)
> Do this FIRST. Every other phase depends on this being solid.

### 1.1 Custom Exception Classes

- [x] Create exception/ package in lms-live-service
- [x] LmsException.java - base exception class
- [x] SessionNotFoundException.java - 404
- [x] SessionAlreadyActiveException.java - 409 (room already running)
- [x] RoomCapacityExceededException.java - 409 (room full)
- [x] LiveKitServerException.java - 503 (LiveKit down)
- [x] RecordingFailedException.java - 500
- [x] UnauthorizedException.java - 403
- [x] InvalidRequestException.java - 400
- [x] ErrorResponse.java DTO added in dto/ package
- [x] Custom exceptions applied in LiveSessionService, LiveSessionController, LiveKitWebhookController

### 1.2 Global Exception Handler

- [x] GlobalExceptionHandler.java with @RestControllerAdvice
- [x] ErrorResponse.java DTO - { errorCode, message, timestamp }
- [x] Handle SessionNotFoundException -> 404
- [x] Handle SessionAlreadyActiveException -> 409
- [x] Handle LiveKitServerException -> 503
- [x] Handle MethodArgumentNotValidException -> 400
- [x] Handle generic Exception -> 500 fallback
- [x] Never expose stack trace to client
- [x] Fixed LiveSessionService.joinSession() method signature to accept JoinRequest object
- [x] All exception files verified and compiled successfully

### 1.3 Request Validation

- [x] Add @Valid to all controller method parameters
- [x] StartSessionRequest - @NotBlank on courseId, instructorId, instructorName
- [x] JoinRequest - @NotBlank on roomName, studentId, studentName + @Size limits
- [x] @Pattern validation on IDs (alphanumeric + dash only)
- [x] Return 400 with field-level error messages
- [x] GlobalExceptionHandler handles MethodArgumentNotValidException with field-level errors
- [x] All validation annotations in place and working

### 1.4 Structured Logging

- [ ] Replace ALL System.out.println with SLF4J log.info / log.warn / log.error
- [ ] Add Logger to every class
- [ ] Every log must include: what happened + roomName + userId
- [ ] Error logs must include exception message
- [ ] Webhook logs: log event type + roomName on every event

### 1.5 Idempotency in Webhooks

- [x] room_finished - check if status already ENDED before updating
- [ ] participant_joined - check if attendance record already exists (TODO Phase 4)
- [ ] participant_left - check if already marked LEFT (TODO Phase 4)
- [x] egress_ended - check if recordingUrl already saved
- [x] Log warning when duplicate event detected

### 1.6 Transactional Safety

- [x] startSession() - if DB save fails after LiveKit room created -> delete room from LiveKit (rollback)
- [x] endSession() - if LiveKit delete fails -> still mark DB as ENDED + log error
- [x] Add tryDeleteRoom() private method for rollback
- [x] Add @Transactional where multiple DB operations happen

---

## PHASE 1.5 - Critical Security (COMPLETED May 20, 2026)
> Moved from Phase 7 — too critical to leave for last

### 1.5.1 Concurrent Session Prevention ✅ DONE
- [x] Instructor cannot start 2 classes simultaneously
- [x] Throws SessionAlreadyActiveException with helpful message
- [x] Logs conflict attempts with roomName

### 1.5.2 Database Indexes ✅ DONE
- [x] @Index on roomName, instructorId+status, courseId
- [x] Improves query performance for common lookups

### 1.5.3 Webhook Security ✅ DONE
- [x] IP whitelist configured in application.yml
- [x] Rejects webhooks from unauthorized IPs (403)
- [x] Supports X-Forwarded-For for proxy setups
- [x] Logs all webhook attempts with caller IP

### 1.5.4 Session Duration Limit ✅ DONE
- [x] scheduledEndTime field added to LiveSession
- [x] Auto-ends sessions after 4 hours (configurable)
- [x] @Scheduled task runs every minute
- [x] Logs auto-end events with reason

---

## PHASE 1.5 - Critical Security (Do Before Any Demo)
> These were in Phase 7 but are too critical to leave for last.

### 1.5.1 Concurrent Session Prevention

- [x] In startSession() - check if instructor already has ACTIVE session:
  findByInstructorIdAndStatus(instructorId, ACTIVE) -> if exists -> throw SessionAlreadyActiveException
- [x] Error: "You already have an active class. Please end it before starting a new one."
- [x] Log every conflict attempt with teacherId + existing roomName

### 1.5.2 Database Indexes - lms-live-service

- [x] Index on live_sessions.room_name
- [x] Composite index on live_sessions.instructor_id + status
- [x] Index on live_sessions.course_id
- [x] Add via @Index annotation on LiveSession entity

### 1.5.3 Webhook Security - IP Whitelist

- [x] LiveKit webhooks only accepted from LiveKit server IP
- [x] Add IP check in LiveKitWebhookController before processing
- [x] If IP not whitelisted -> return 403 + log warning with caller IP
- [x] For local dev: whitelist 127.0.0.1 and localhost
- [x] Configure allowed IPs in application.yml

### 1.5.4 Session Duration Limit

- [x] Add scheduledEndTime field to LiveSession entity
- [x] When class starts -> set scheduledEndTime = startTime + maxDuration
- [x] @Scheduled task - every minute check sessions past end time
- [x] If now() > scheduledEndTime -> auto-call endSession(roomName)
- [x] Log every auto-end with roomName + reason


---

## PHASE 2 - lms-recording-service (New Microservice)
> Port: 8085
> Independent microservice - build and test separately before integrating

### 2.1 Project Setup

- [ ] Create new Spring Boot project lms-recording-service (port 8085)
- [ ] Dependencies: spring-web, spring-data-jpa, h2, livekit-server SDK, lombok, validation
- [ ] application.yml - LiveKit keys, recordings folder path
- [ ] CorsConfig.java
- [ ] LiveKitEgressConfig.java - EgressClient bean

### 2.2 DB Model

- [ ] Recording.java entity:
  id, instituteId, courseId, roomName, teacherId,
  egressId, filePath, fileName,
  status (STARTING / RECORDING / STOPPED / FAILED),
  startTime, stopTime, durationSeconds, fileSizeBytes, quality
- [ ] RecordingRepository.java - findByRoomName, findByCourseId, findByInstituteId
- [ ] DB indexes: room_name, course_id, egress_id

### 2.3 Recording Controller

- [ ] POST /api/v1/recording/start - body: { roomName, courseId, instituteId, quality }
- [ ] POST /api/v1/recording/stop - body: { roomName }
- [ ] GET /api/v1/recording/list?courseId=xxx&page=0&size=20
- [ ] GET /api/v1/recording/list?instituteId=xxx&page=0&size=20
- [ ] GET /api/v1/recording/{id}
- [ ] DELETE /api/v1/recording/{id}

### 2.4 Recording Service Logic

- [ ] startRecording(roomName, courseId, quality):
  - Call EgressClient to start Room Composite Egress
  - Output: local file /recordings/{instituteId}/{courseId}/{roomName}/{timestamp}.mp4
  - Save egressId + status RECORDING to DB
  - If EgressClient fails -> throw RecordingFailedException (class continues)
- [ ] stopRecording(roomName):
  - Find active recording by roomName
  - Call EgressClient.stopEgress(egressId)
  - Update status STOPPED in DB
  - If already stopped -> log warning, return gracefully (idempotent)
- [ ] handleEgressEnded(egressId, filePath, duration):
  - Update filePath, durationSeconds, fileSizeBytes, status=STOPPED
- [ ] handleEgressFailed(egressId, errorMessage):
  - Update status=FAILED, log error, do NOT crash

### 2.5 Webhook Handler (Recording Service)

- [ ] POST /api/v1/recording/webhook - receive LiveKit egress events
- [ ] Verify webhook signature (HMAC)
- [ ] Handle egress_started -> update status to RECORDING
- [ ] Handle egress_ended -> save filePath, duration, mark STOPPED
- [ ] Handle egress_updated -> log progress
- [ ] Idempotency check before every update

### 2.6 File Serving

- [ ] GET /api/v1/recording/file/{id} - serve MP4 as stream
- [ ] Support Range header for video seek (partial content 206)
- [ ] Return 404 if file not found on disk

### 2.7 Exception Handling

- [ ] RecordingNotFoundException -> 404
- [ ] RecordingAlreadyActiveException -> 409
- [ ] RecordingFailedException -> 500
- [ ] EgressServiceUnavailableException -> 503
- [ ] GlobalExceptionHandler with ErrorResponse DTO
- [ ] All exceptions logged with roomName + egressId

### 2.8 Integration - lms-live-service calls Recording Service

- [ ] Add WebClient bean in lms-live-service
- [ ] In webhook room_started -> WebClient POST to /api/v1/recording/start
- [ ] In webhook room_finished -> WebClient POST to /api/v1/recording/stop
- [ ] If recording-service is down -> log error, do NOT fail the class
- [ ] Timeout: 3 seconds max for WebClient call
- [ ] Add X-Internal-Secret header to every WebClient call
- [ ] Recording service verifies X-Internal-Secret before processing

### 2.9 Egress Docker Setup

- [ ] Document Docker command for Egress service
- [ ] Mount local /recordings folder as volume
- [ ] Egress connects to LiveKit server on ws://localhost:7880

### 2.10 Disk Space Management

- [ ] diskSpaceMonitor() scheduled task - runs every hour
- [ ] If disk < 20% free -> log warning
- [ ] If disk < 5% free -> block new recordings, return 507 Insufficient Storage
- [ ] GET /api/v1/recording/storage-stats endpoint
- [ ] Max storage limit per institute in plan config
- [ ] Before new recording -> check institute storage quota

---

## PHASE 3 - Core Features (lms-live-service + Frontend)
> lms-live-service already has basic start/join/end. Now add missing features.

### 3.1 Room Name Display (Frontend - Quick Win)

- [ ] After instructor starts class -> show roomName prominently
- [ ] Add "Copy Room Name" button (copies to clipboard)
- [ ] Show: "Share this room name with your students"
- [ ] Room name in memory only (NOT localStorage)

### 3.2 API Versioning - lms-live-service

- [ ] Change all endpoints from /api/live/ to /api/v1/live/
- [ ] Update frontend liveApi.js to use /api/v1/ prefix
- [ ] Update WebClient calls to use /api/v1/ prefix

### 3.3 Chat - Real-time During Class

- [ ] No backend needed - LiveKit Data Channel handles it
- [ ] ChatPanel.jsx - sidebar with message list + input
- [ ] Use useDataChannel hook from LiveKit React SDK
- [ ] Send message -> broadcast to all participants
- [ ] Receive messages -> display with sender name + timestamp
- [ ] Unread message count badge when panel closed
- [ ] Messages in memory only - cleared when class ends

### 3.4 Participant List

Backend:
- [ ] GET /api/v1/live/participants?roomName=xxx
- [ ] Call roomServiceClient.listParticipants(roomName)
- [ ] Return list of { identity, name, isPublishing, joinedAt }

Frontend:
- [ ] ParticipantPanel.jsx - list of participants
- [ ] Show participant count in header
- [ ] Use useParticipants() hook (real-time)
- [ ] Show mic/camera status icon per participant

### 3.5 Mute / Kick (Instructor Controls)

Backend:
- [ ] POST /api/v1/live/mute - body: { roomName, participantIdentity, trackSid }
- [ ] POST /api/v1/live/kick - body: { roomName, participantIdentity }
- [ ] Validate caller has roomAdmin permission before allowing
- [ ] Call roomServiceClient.mutePublishedTrack() for mute
- [ ] Call roomServiceClient.removeParticipant() for kick
- [ ] Log every mute/kick with who did it

Frontend:
- [ ] Mute/kick buttons next to each participant (instructor view only)
- [ ] Confirm dialog before kick
- [ ] Toast notification after success


---

## PHASE 4 - UX and Polish

### 4.1 Raise Hand

- [ ] "Raise Hand" button for students
- [ ] Use LiveKit Data Channel to broadcast raise hand event
- [ ] Instructor sees raised hand indicator next to student name
- [ ] Instructor can lower hand (dismiss)
- [ ] Visual indicator on student tile when hand is raised

### 4.2 Attendance Tracking (lms-live-service)

- [ ] Attendance.java entity - (roomName, participantIdentity, participantName, joinTime, leaveTime, durationSeconds)
- [ ] AttendanceRepository.java
- [ ] DB indexes: room_name, participant_identity + room_name
- [ ] In webhook participant_joined -> save attendance record with joinTime
- [ ] In webhook participant_left -> update leaveTime + calculate duration
- [ ] GET /api/v1/live/attendance?roomName=xxx&page=0&size=50
- [ ] Handle edge case: participant_left without participant_joined

### 4.3 Quality Control

Backend:
- [ ] Add enrolledCount field to StartSessionRequest
- [ ] In startSession() - calculate quality based on enrolled count:
  1-100 students -> 720p
  101-300 students -> 540p
  301-500 students -> 360p
  500+ students -> 240p
- [ ] Store quality in room metadata when creating LiveKit room
- [ ] Return quality in JoinResponse

Frontend:
- [ ] Read quality from JoinResponse
- [ ] Apply video constraints when publishing (instructor)
- [ ] Show quality badge in UI
- [ ] Enable dynacast: true in LiveKitRoom options
  - Automatically stops sending a video stream when no student is watching it
  - Saves bandwidth when room is empty or students have slow internet
- [ ] Enable simulcast: true in publishDefaults
  - Instructor sends 3 quality streams (180p, 360p, 720p) simultaneously
  - LiveKit picks the right one per student based on their network
  - Dynacast + Simulcast together = smart bandwidth management

### 4.4 Recording Indicator (Frontend)

- [ ] Show REC badge in top corner when recording is active
- [ ] Both instructor and students can see it

### 4.5 Recordings Page

Backend (lms-recording-service already has this from Phase 2):
- [ ] Confirm GET /api/v1/recording/list?courseId=xxx works with pagination

Frontend:
- [ ] RecordingsPage.jsx - list of past class recordings
- [ ] Show: class date, duration, course name
- [ ] Play button -> open video player
- [ ] Video served from recording service file endpoint

### 4.6 Live Participant Count (Frontend)

- [ ] Show student count in header during class
- [ ] Update in real-time using useParticipants() hook

### 4.7 Frontend Error Handling (Per Status Code)

- [ ] 404 -> "Class has not started yet. Please wait for instructor."
- [ ] 409 -> "Class is full. Please contact your instructor."
- [ ] 503 -> "Video server is temporarily down. Retrying in 3 seconds..." + auto retry
- [ ] 400 -> Show field-level validation errors
- [ ] Network error -> "No internet connection."
- [ ] Token expired -> "Session expired. Please rejoin."

### 4.8 Retry Logic (Frontend)

- [ ] On 503 -> auto retry after 3 seconds (max 3 attempts)
- [ ] Show retry countdown to user
- [ ] After 3 failed retries -> "Please try again later"

### 4.9 Frontend Reconnection Handling

- [ ] Handle onReconnecting event -> show "Reconnecting..." overlay
- [ ] Handle onReconnected event -> hide overlay, restore UI state
- [ ] Handle onDisconnected with reason:
  KICKED -> "You were removed from the class by the instructor"
  ROOM_DELETED -> "The class has ended"
  NETWORK_ERROR -> "Connection lost. Attempting to reconnect..."
  TOKEN_EXPIRED -> "Session expired. Please rejoin."
- [ ] Preserve chat messages during reconnection
- [ ] Preserve raise hand state during reconnection
- [ ] Show connection quality indicator

---

## PHASE 5 - Production Hardening (All 3 Services)

### 5.1 Health Check - All Services

- [ ] Add Spring Boot Actuator to lms-live-service
- [ ] Add Spring Boot Actuator to lms-recording-service
- [ ] Add Spring Boot Actuator to lms-schedule-service
- [ ] Custom health indicator in lms-live-service: check LiveKit server reachable
- [ ] Custom health indicator in lms-live-service: check recording-service reachable
- [ ] Custom health indicator in lms-live-service: check schedule-service reachable
- [ ] Custom health indicator in lms-recording-service: check Egress Docker reachable
- [ ] All return structured health response with component status

### 5.2 API Documentation - All Services

- [ ] Add springdoc-openapi to lms-live-service
- [ ] Add springdoc-openapi to lms-recording-service
- [ ] Add springdoc-openapi to lms-schedule-service
- [ ] Document all endpoints with request/response examples
- [ ] Swagger UI at /swagger-ui.html on each service

### 5.3 Rate Limiting

- [ ] Limit /api/v1/live/join - max 10 requests per minute per IP
- [ ] Limit /api/v1/live/start - max 5 requests per minute per instructorId
- [ ] Return 429 Too Many Requests with retry-after header

### 5.4 Request/Response Audit Logging - All Services

- [ ] AuditLogFilter.java - Spring OncePerRequestFilter
- [ ] Log every request: method + endpoint + userId + IP + timestamp
- [ ] Log every response: status code + response time ms
- [ ] Skip: /actuator/health, /h2-console
- [ ] Apply to all 3 microservices

### 5.5 Environment Config

- [ ] application.yml - replace hardcoded keys with env variables + fallback:
  livekit.api.key: ${LIVEKIT_API_KEY:devkey}
  livekit.api.secret: ${LIVEKIT_API_SECRET:devsecret}
- [ ] Frontend .env file: VITE_BACKEND_URL, VITE_LIVEKIT_URL
- [ ] Create .env.example for frontend and each backend service
- [ ] Add .env to .gitignore

### 5.6 Docker Compose - One Command Setup

- [ ] docker-compose.yml at project root:
  LiveKit Server container
  LiveKit Egress container
  lms-live-service container
  lms-recording-service container
  lms-schedule-service container
- [ ] .env file - all keys in one place
- [ ] .env.example - template for new developers
- [ ] Service startup order: LiveKit first -> then Spring Boot services
- [ ] healthcheck on LiveKit container

### 5.7 Circuit Breaker - Resilience4j

- [ ] Add resilience4j-spring-boot3 to lms-live-service
- [ ] Circuit breaker on WebClient call to recording-service:
  After 3 failures -> open circuit -> skip recording (class continues)
  After 30 seconds -> half-open -> try again
- [ ] Circuit breaker on WebClient call to schedule-service:
  After 3 failures -> open circuit -> allow class with warning log
- [ ] Fallback method for each circuit breaker

### 5.8 Graceful Shutdown

- [ ] ApplicationListener<ContextClosedEvent> in lms-live-service
- [ ] On shutdown -> find all ACTIVE sessions -> mark as INTERRUPTED
- [ ] Add INTERRUPTED to SessionStatus enum
- [ ] Log: "Service shutting down - marking X active sessions as INTERRUPTED"
- [ ] lms-recording-service: on shutdown -> stop active egress gracefully

### 5.9 README and Developer Docs

- [ ] README.md at project root
- [ ] Prerequisites, how to run locally, all service URLs
- [ ] Environment variables list
- [ ] How to run with Docker Compose
- [ ] Troubleshooting section


---

## PHASE 6 - lms-schedule-service (New Microservice)
> Port: 8086 | Most complex service - build last when everything else is stable

### 6.1 Project Setup

- [ ] Create new Spring Boot project lms-schedule-service (port 8086)
- [ ] Dependencies: spring-web, spring-data-jpa, h2, lombok, validation
- [ ] application.yml - DB config, port
- [ ] CorsConfig.java

### 6.2 DB Models

- [ ] InstitutePlan.java entity:
  id, instituteId, planName (BASIC/PRO/ENTERPRISE),
  totalBandwidthMbps, maxStudentsTotal, maxConcurrentClasses,
  maxStorageGB, timezone,
  planStartDate, planEndDate, isActive
- [ ] ScheduledSession.java entity:
  id, instituteId, teacherId, teacherName, subject, courseId,
  startTime (UTC), endTime (UTC), instituteTimezone,
  studentCount, quality, bandwidthReservedMbps,
  status (SCHEDULED/ACTIVE/COMPLETED/CANCELLED),
  recurrenceType (ONCE/WEEKLY/DAILY), recurrenceEndDate, parentScheduleId,
  roomName, createdAt, updatedAt
  @Version for optimistic locking
- [ ] InstitutePlanRepository.java - findByInstituteId, findByInstituteIdForUpdate (pessimistic lock)
- [ ] ScheduledSessionRepository.java:
  findOverlappingSessions(instituteId, startTime, endTime) - JPQL
  findByInstituteIdAndWeek(instituteId, weekStart, weekEnd)
  findByTeacherIdAndDate(teacherId, date)
- [ ] DB indexes:
  Composite on institute_id + start_time + end_time
  Index on teacher_id + start_time
  Index on status

### 6.3 Bandwidth Calculation Logic

- [ ] BandwidthCalculator.java utility class:
  360p  -> studentCount x 0.5 Mbps
  540p  -> studentCount x 1.0 Mbps
  720p  -> studentCount x 2.5 Mbps
  1080p -> studentCount x 4.0 Mbps
- [ ] getReservedBandwidth(instituteId, startTime, endTime)
- [ ] getAvailableBandwidth(instituteId, startTime, endTime)
- [ ] canBook() returns { canBook, availableMbps, requestedMbps, message }

### 6.4 Schedule Controller

- [ ] POST /api/v1/schedule/book
- [ ] GET /api/v1/schedule/week?instituteId=xxx&weekStart=2026-05-19
- [ ] GET /api/v1/schedule/teacher?teacherId=xxx&date=2026-05-19
- [ ] DELETE /api/v1/schedule/{id}?deleteAll=false
- [ ] DELETE /api/v1/schedule/{id}?deleteAll=true (cancel all recurring)
- [ ] GET /api/v1/schedule/capacity?instituteId=xxx&startTime=xxx&endTime=xxx
- [ ] PATCH /api/v1/schedule/{id}/activate
- [ ] PATCH /api/v1/schedule/{id}/complete
- [ ] GET /api/v1/schedule/permission?instituteId=xxx&courseId=xxx&teacherId=xxx

### 6.5 Booking Logic

- [ ] bookSlot(request):
  1. Validate request
  2. Check institute plan exists and is active
  3. Check slot duration max 4 hours
  4. Lock InstitutePlan row (pessimistic lock) to prevent race condition
  5. Find overlapping sessions
  6. Calculate reserved + requested bandwidth
  7. If over limit -> throw InsufficientBandwidthException with helpful message
  8. If studentCount > plan max -> throw StudentLimitExceededException
  9. Save ScheduledSession
  10. Return booking confirmation
- [ ] Helpful error: "Available: 700 Mbps, Required: 1000 Mbps. Max 700 students at 540p."

### 6.6 Recurring Schedule

- [ ] generateRecurringSessions(booking) for WEEKLY/DAILY recurrence
- [ ] Check bandwidth for EACH generated session before saving
- [ ] Report which dates failed bandwidth check
- [ ] parentScheduleId links recurring sessions to original

### 6.7 Permission Check Integration

- [ ] GET /api/v1/schedule/permission called by lms-live-service before room creation
- [ ] Returns { allowed, scheduleId, reason }
- [ ] lms-live-service: if allowed=false -> return 403 to instructor with reason
- [ ] After room created -> PATCH activate
- [ ] After room ended -> PATCH complete

### 6.8 Time Zone Handling

- [ ] All times stored in DB as UTC
- [ ] Accept booking times in institute local timezone, convert to UTC before saving
- [ ] API responses return both UTC and local time:
  startTimeUtc, startTimeLocal, timezone
- [ ] Weekly schedule view displays in institute timezone
- [ ] Auto-end scheduler compares now() UTC vs scheduledEndTime UTC
- [ ] Frontend displays in browser timezone using Intl.DateTimeFormat

### 6.9 Exception Handling

- [ ] InsufficientBandwidthException -> 409
- [ ] StudentLimitExceededException -> 409
- [ ] SlotConflictException -> 409 (teacher already has class at same time)
- [ ] PlanNotFoundException -> 404
- [ ] PlanExpiredException -> 402
- [ ] InvalidTimeSlotException -> 400
- [ ] GlobalExceptionHandler with ErrorResponse DTO

### 6.10 Weekly Schedule View

- [ ] Return 7-day schedule grouped by day
- [ ] Show daily bandwidth usage summary
- [ ] Highlight slots where bandwidth > 80% (warning)
- [ ] Highlight slots where bandwidth = 100% (full)

### 6.11 Frontend - Schedule Pages

- [ ] WeeklySchedulePage.jsx - calendar/grid view
- [ ] BookSlotPage.jsx - form to book a slot
- [ ] Bandwidth availability indicator per slot (green/yellow/red)
- [ ] CapacityBar.jsx - visual bandwidth usage bar
- [ ] Teacher view - own classes only
- [ ] Institute admin view - all classes

### 6.12 Class Joining Time Window

- [ ] Allow join: scheduledStartTime - 15 min to scheduledEndTime
- [ ] Too early -> 403: "Class starts at {time}. You can join 15 minutes before."
- [ ] Too late -> 403: "This class has ended. Recording will be available soon."
- [ ] GET /api/v1/schedule/join-window?courseId=xxx
- [ ] Instructor exempt from time window

### 6.13 Production Rules for Schedule Service

- [ ] All booking operations @Transactional
- [ ] Pessimistic locking on InstitutePlan during booking
- [ ] Log every booking attempt with instituteId + teacherId + time + result


---

## PHASE 7 - Remaining Security and Hardening

### 7.1 Security - Service-to-Service Auth

- [ ] X-Internal-Secret header in all WebClient calls (live -> recording, live -> schedule)
- [ ] Recording service verifies X-Internal-Secret before processing
- [ ] Schedule service verifies X-Internal-Secret before processing
- [ ] Configure secret in application.yml via env variable

### 7.2 Input Sanitization - All Services

- [ ] Strip HTML/script tags from all string inputs
- [ ] Trim whitespace from all string fields before saving to DB
- [ ] Normalize roomName - lowercase, replace spaces with dashes
- [ ] Max length on all string fields @Column(length=100)
- [ ] Reject null bytes and control characters

### 7.3 Pagination - All List APIs

- [ ] All list APIs use Spring Pageable + Page<T>
- [ ] Response format: { content, totalElements, totalPages, currentPage, pageSize }
- [ ] Never return unbounded list

### 7.4 Notification System (Basic)

- [ ] NotificationService.java in lms-live-service
- [ ] In-memory notification store per userId
- [ ] GET /api/v1/notifications?userId=xxx
- [ ] Triggers:
  Class starts -> notify enrolled students
  Recording ready -> notify students
  Class auto-ending in 5 min -> notify instructor
- [ ] PATCH /api/v1/notifications/{id}/read
- [ ] Frontend: poll every 30 seconds
- [ ] Notification bell icon with unread count

---

## PHASE 8 - Edge Cases and Reliability

### 8.1 Institute Data Isolation

- [ ] Every query in schedule-service includes instituteId filter
- [ ] Every query in recording-service includes instituteId filter
- [ ] Every query in live-service includes instituteId filter where applicable
- [ ] Add instituteId to LiveSession entity
- [ ] Validate in every API: instituteId in request matches resource being accessed
- [ ] Mismatch -> 403 Forbidden (not 404 - do not reveal other institute data exists)

### 8.2 Bandwidth Race Condition

- [ ] Use SELECT FOR UPDATE (pessimistic locking) on InstitutePlan during booking
- [ ] @Lock(LockModeType.PESSIMISTIC_WRITE) on findByInstituteIdForUpdate
- [ ] Wrap entire bookSlot() in @Transactional with this lock
- [ ] Second concurrent request waits -> re-checks bandwidth -> fails if no longer available
- [ ] Test: 2 simultaneous booking requests for same slot -> verify only 1 succeeds

### 8.3 Health Check Cross-Service

- [ ] lms-live-service health reports recording-service status
- [ ] lms-live-service health reports schedule-service status
- [ ] lms-live-service health reports LiveKit server status
- [ ] lms-recording-service health reports Egress Docker status
- [ ] Structured response with all component statuses

### 8.4 Graceful Shutdown (Already in Phase 5 - verify here)

- [ ] Confirm INTERRUPTED status added to SessionStatus enum
- [ ] Confirm @PreDestroy or ContextClosedEvent handler marks sessions INTERRUPTED
- [ ] Confirm recording service stops active egress on shutdown

### 8.5 Recording Disk Space Management (Already in Phase 2 - verify here)

- [ ] Confirm diskSpaceMonitor scheduled task is running
- [ ] Confirm 507 response when disk < 5%
- [ ] Confirm storage-stats endpoint works

### 8.6 Class Joining Time Window (Already in Phase 6 - verify here)

- [ ] Confirm join window check works end to end
- [ ] Test: student tries to join 1 hour before class -> gets correct error
- [ ] Test: student tries to join after class ended -> gets correct error

### 8.7 Time Zone End-to-End Test

- [ ] Confirm times stored as UTC in DB
- [ ] Confirm API returns both UTC and local time
- [ ] Confirm auto-end scheduler uses UTC comparison
- [ ] Confirm frontend displays in browser timezone

---

## PRODUCTION RULES - Always Follow

### Backend Rules
```
1.  NEVER use System.out.println - always use log.info/warn/error
2.  NEVER throw generic RuntimeException - use custom exceptions
3.  NEVER return stack trace to client - use ErrorResponse DTO
4.  ALWAYS validate request body with @Valid
5.  ALWAYS handle LiveKit call failures - wrap in try/catch
6.  ALWAYS check idempotency in webhook handlers
7.  ALWAYS rollback LiveKit changes if DB fails
8.  ALWAYS add @Transactional where multiple DB operations happen
9.  NEVER hardcode API keys - use application.yml + env variables
10. ALWAYS log: what happened + roomName + userId + timestamp
11. ALWAYS scope queries to instituteId - never return cross-institute data
12. ALWAYS use /api/v1/ prefix on all endpoints
```

### Frontend Rules
```
1. NEVER store token in localStorage - keep in React state only
2. NEVER show raw error messages - show friendly messages
3. ALWAYS handle loading state - show spinner during API calls
4. ALWAYS handle network errors - not just HTTP errors
5. ALWAYS confirm before destructive actions (kick, end class)
6. ALWAYS show feedback after actions (toast notifications)
7. NEVER expose roomName in URL - keep in state
8. ALWAYS disable buttons during loading to prevent double-submit
```

### API Design Rules
```
200 -> success
400 -> bad request (validation failed)
401 -> unauthorized
403 -> forbidden (not allowed or wrong institute)
404 -> not found
409 -> conflict (already exists / room full / bandwidth exceeded)
429 -> too many requests
500 -> internal server error
503 -> service unavailable (LiveKit down)
507 -> insufficient storage (disk full)

Error response format - always consistent:
{ "errorCode": "SESSION_NOT_FOUND", "message": "...", "timestamp": "..." }

Never return null - return empty list [] or 404
Always return roomName in start session response
All endpoints use /api/v1/ prefix
```

---

## SERVICE COMMUNICATION MAP

```
LiveKit-Frontend (3000)
        |
        | HTTP REST /api/v1/
        v
lms-live-service (8084)
        |
        |-- WebClient + X-Internal-Secret --> lms-recording-service (8085)
        |-- WebClient + X-Internal-Secret --> lms-schedule-service (8086)
        |
        | LiveKit Server API (HTTP)
        v
LiveKit Server (7880)
        |
        | Webhooks (HTTP POST)
        v
lms-live-service /api/v1/live/webhook
lms-recording-service /api/v1/recording/webhook

LiveKit Egress (Docker)
        |
        | Saves files to
        v
/recordings/ folder (local disk)
        |
        | Served by
        v
lms-recording-service GET /api/v1/recording/file/{id}
```

---

## FINAL PROGRESS SUMMARY

| Phase | Service | Tasks | Done | Left |
|-------|---------|-------|------|------|
| Already Built | live-service + Frontend | 22 | 22 | 0 |
| Phase 1 - Foundation | lms-live-service | 24 | 0 | 24 |
| Phase 1.5 - Critical Security | lms-live-service | 12 | 0 | 12 |
| Phase 2 - Recording Service | lms-recording-service | 38 | 0 | 38 |
| Phase 3 - Core Features | lms-live-service + Frontend | 22 | 0 | 22 |
| Phase 4 - UX and Polish | Frontend + lms-live-service | 24 | 0 | 24 |
| Phase 5 - Production Hardening | All 3 services | 28 | 0 | 28 |
| Phase 6 - Schedule Service | lms-schedule-service | 48 | 0 | 48 |
| Phase 7 - Remaining Security | All services | 16 | 0 | 16 |
| Phase 8 - Edge Cases | All services | 18 | 0 | 18 |
| TOTAL | | 252 | 22 | 230 |

---

*Last Updated: May 19, 2026*
*Update this file after every completed task*
