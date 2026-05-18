# LMS LiveKit — Build Tracker & Production Checklist
**Project:** lms-live-service + lms-recording-service + lms-schedule-service + LiveKit-Frontend
**Goal:** Production / Company Level Code
**Last Updated:** May 18, 2026

---

## 🗂️ Microservices Overview

| Service | Port | Status | Responsibility |
|---------|------|--------|---------------|
| `lms-live-service` | 8084 | 🟡 Half Built | LiveKit room management, token generation, webhooks |
| `lms-recording-service` | 8085 | ❌ Not Started | Egress recording, file storage, recording list |
| `lms-schedule-service` | 8086 | ❌ Not Started | Weekly schedule, slot booking, bandwidth reservation |
| `LiveKit-Frontend` | 3000 | 🟡 Half Built | React UI for instructor + student |
| `LiveKit Server` | 7880 | ✅ Docker | SFU video routing |
| `LiveKit Egress` | - | ❌ Not Started | Recording Docker container |

---

## 📋 How to Use This File
- `[x]` = Done ✅
- `[ ]` = Not done yet ❌
- Update this file after every task is completed

---

## ✅ WHAT IS ALREADY BUILT

### Backend — lms-live-service

- [x] Spring Boot project setup (port 8084)
- [x] H2 in-memory database configured
- [x] LiveKit Java SDK integrated (v0.12.1)
- [x] `LiveKitConfig.java` — RoomServiceClient bean
- [x] `CorsConfig.java` — CORS configured
- [x] `LiveSession.java` — Entity (roomName, courseId, instructorId, status, startTime, endTime, recordingUrl)
- [x] `LiveSessionRepository.java` — JPA repository
- [x] `LiveKitTokenService.java` — Instructor token (canPublish, roomAdmin) + Student token (subscribe only)
- [x] `LiveSessionService.java` — startSession, joinSession, endSession, isRoomActive
- [x] `LiveSessionController.java` — POST /start, POST /join, POST /end, GET /status
- [x] `LiveKitWebhookController.java` — room_started, room_finished, participant_joined, participant_left, egress_ended
- [x] `StartSessionRequest.java` DTO
- [x] `JoinRequest.java` DTO
- [x] `JoinResponse.java` DTO
- [x] application.yml — LiveKit keys, H2 config, server port

### Frontend — LiveKit-Frontend

- [x] React + Vite project setup
- [x] LiveKit React SDK integrated
- [x] `RoleSelector.jsx` — Choose Instructor or Student
- [x] `InstructorLobby.jsx` — Start class form
- [x] `StudentLobby.jsx` — Join class form (roomName, studentId, studentName)
- [x] `VideoConference.jsx` — Grid layout, ControlBar, Audio renderer, Leave button
- [x] `App.jsx` — Screen navigation (role → lobby → room)
- [x] `liveApi.js` — startSession, joinSession, endSession API calls

---

## ❌ WHAT NEEDS TO BE BUILT

---

## PHASE 1 — Foundation (Do This First — Everything Depends on It)

> **Rule:** Bina foundation ke aage mat badho. Yeh sab production code ka base hai.

### 1.1 Custom Exception Classes (Backend)

- [ ] Create `exception/` package
- [ ] `LmsException.java` — base exception class
- [ ] `SessionNotFoundException.java` — 404 (room not found)
- [ ] `SessionAlreadyActiveException.java` — 409 (room already running)
- [ ] `RoomCapacityExceededException.java` — 409 (room full)
- [ ] `LiveKitServerException.java` — 503 (LiveKit down/unreachable)
- [ ] `RecordingFailedException.java` — 500 (recording start/stop failed)
- [ ] `UnauthorizedException.java` — 403 (not allowed)
- [ ] `InvalidRequestException.java` — 400 (bad input)

### 1.2 Global Exception Handler (Backend)

- [ ] Create `exception/GlobalExceptionHandler.java` with `@RestControllerAdvice`
- [ ] `ErrorResponse.java` DTO — `{ errorCode, message, timestamp }`
- [ ] Handle `SessionNotFoundException` → 404
- [ ] Handle `SessionAlreadyActiveException` → 409
- [ ] Handle `RoomCapacityExceededException` → 409
- [ ] Handle `LiveKitServerException` → 503
- [ ] Handle `RecordingFailedException` → 500
- [ ] Handle `MethodArgumentNotValidException` → 400 (validation errors)
- [ ] Handle generic `Exception` → 500 (fallback)
- [ ] Never expose internal stack trace to client

### 1.3 Request Validation (Backend)

- [ ] Add `@Valid` to all controller method parameters
- [ ] `StartSessionRequest` — `@NotBlank` on courseId, instructorId, instructorName
- [ ] `JoinRequest` — `@NotBlank` on roomName, studentId, studentName + `@Size` limits
- [ ] Add `@Pattern` validation on IDs (alphanumeric + dash only)
- [ ] Return 400 with field-level error messages on validation failure

### 1.4 Structured Logging (Backend)

- [ ] Replace ALL `System.out.println` with SLF4J `log.info / log.warn / log.error`
- [ ] Add `private static final Logger log = LoggerFactory.getLogger(ClassName.class)` to every class
- [ ] Every log must include: what happened + roomName/sessionId + userId
- [ ] Error logs must include exception message
- [ ] Webhook logs: log event type + roomName on every event received

### 1.5 Idempotency in Webhooks (Backend)

- [ ] `room_finished` — check if status already ENDED before updating
- [ ] `participant_joined` — check if attendance record already exists
- [ ] `participant_left` — check if already marked LEFT
- [ ] `egress_ended` — check if recordingUrl already saved
- [ ] Log warning when duplicate event detected

### 1.6 Transactional Safety (Backend)

- [ ] `startSession()` — if DB save fails after LiveKit room created → delete room from LiveKit (rollback)
- [ ] `endSession()` — if LiveKit delete fails → still mark DB as ENDED + log error
- [ ] `startRecording()` — if egress start fails → log error, do NOT fail the class
- [ ] Add `tryDeleteRoom()` private method for rollback
- [ ] Add `@Transactional` where DB operations span multiple steps

---

## PHASE 2 — Core Features

### 2.1 Room Name Display (Frontend — Quick Win, 30 min)

- [ ] After instructor starts class → show roomName prominently on screen
- [ ] Add "Copy Room Name" button (copies to clipboard)
- [ ] Show instruction: "Share this room name with your students"
- [ ] Room name should be in memory only (NOT localStorage)

### 2.2 Recording Service — New Microservice

- [ ] Create new Spring Boot project `lms-recording-service` (port 8085)
- [ ] Add LiveKit Java SDK dependency
- [ ] `RecordingController.java` — POST /api/recording/start, POST /api/recording/stop, GET /api/recording/list
- [ ] `RecordingService.java` — EgressClient integration
- [ ] `Recording.java` — Entity (roomName, courseId, egressId, filePath, status, startTime, endTime, durationSeconds)
- [ ] `RecordingRepository.java`
- [ ] Auto-start recording when `room_started` webhook received (via WebClient call from lms-live-service)
- [ ] Auto-stop recording when `room_finished` webhook received (via WebClient call from lms-live-service)
- [ ] Save recording to local folder `/recordings/{courseId}/{roomName}/`
- [ ] Save file path to DB after `egress_ended` webhook
- [ ] GET /api/recording/list?courseId=xxx — return list of recordings for a course
- [ ] Error handling — if recording fails, class continues (recording is non-critical)
- [ ] Add WebClient bean in lms-live-service to call recording service

### 2.3 Chat — Real-time During Class

**Backend:**
- [ ] No backend needed — LiveKit Data Channel handles it directly

**Frontend:**
- [ ] `ChatPanel.jsx` — sidebar panel with message list + input box
- [ ] Use `useDataChannel` hook from LiveKit React SDK
- [ ] Send message → broadcast to all participants in room
- [ ] Receive messages → display with sender name + timestamp
- [ ] Show unread message count badge when panel is closed
- [ ] Chat panel toggle button in control bar area
- [ ] Messages in memory only — cleared when class ends

### 2.4 Participant List (Frontend + Backend)

**Backend:**
- [ ] Add `GET /api/live/participants?roomName=xxx` endpoint
- [ ] Call `roomServiceClient.listParticipants(roomName)` 
- [ ] Return list of `{ identity, name, isPublishing, joinedAt }`

**Frontend:**
- [ ] `ParticipantPanel.jsx` — show list of participants
- [ ] Show participant count in header ("12 students in class")
- [ ] Use LiveKit `useParticipants()` hook (real-time, no API call needed)
- [ ] Show mic/camera status icon per participant

### 2.5 Mute / Kick (Instructor Controls)

**Backend:**
- [ ] `POST /api/live/mute` — body: `{ roomName, participantIdentity, trackSid }`
- [ ] `POST /api/live/kick` — body: `{ roomName, participantIdentity }`
- [ ] Only instructor can call these (validate instructorId in request)
- [ ] Call `roomServiceClient.mutePublishedTrack()` for mute
- [ ] Call `roomServiceClient.removeParticipant()` for kick
- [ ] Log every mute/kick action with who did it and why

**Frontend:**
- [ ] Show mute/kick buttons next to each participant (instructor view only)
- [ ] Confirm dialog before kick ("Are you sure you want to remove this student?")
- [ ] Show toast notification after mute/kick success

---

## PHASE 3 — UX & Polish

### 3.1 Raise Hand

**Frontend:**
- [ ] "Raise Hand ✋" button for students
- [ ] Use LiveKit Data Channel to broadcast raise hand event
- [ ] Instructor sees raised hand indicator next to student name
- [ ] Instructor can "lower hand" (dismiss)
- [ ] Visual indicator on student tile when hand is raised

### 3.2 Attendance Tracking (Backend)

- [ ] Create `Attendance.java` entity — (roomName, participantIdentity, participantName, joinTime, leaveTime, durationSeconds)
- [ ] `AttendanceRepository.java`
- [ ] In webhook `participant_joined` → save attendance record with joinTime
- [ ] In webhook `participant_left` → update leaveTime + calculate duration
- [ ] `GET /api/live/attendance?roomName=xxx` — return attendance list
- [ ] Handle edge case: participant_left without participant_joined (late webhook)

### 3.3 Quality Control (Backend + Frontend)

**Backend:**
- [ ] Add `enrolledCount` field to `StartSessionRequest`
- [ ] In `startSession()` — calculate quality based on enrolled count:
  - 1-100 students → 720p
  - 101-300 students → 540p
  - 301-500 students → 360p
  - 500+ students → 240p
- [ ] Store quality in room metadata when creating LiveKit room
- [ ] Return quality in `JoinResponse`

**Frontend:**
- [ ] Read quality from JoinResponse
- [ ] Apply video constraints when publishing (instructor)
- [ ] Show current quality badge in UI ("📹 720p")

### 3.4 Recording Indicator (Frontend)

- [ ] Show "🔴 REC" badge in top corner when recording is active
- [ ] Instructor can see recording status
- [ ] Students can see recording status

### 3.5 Recordings Page (Frontend + Backend)

**Backend:**
- [ ] `GET /api/recording/list?courseId=xxx` — list all recordings for a course
- [ ] Return: `{ id, roomName, courseId, filePath, durationSeconds, recordedAt }`

**Frontend:**
- [ ] `RecordingsPage.jsx` — list of past class recordings
- [ ] Show: class date, duration, course name
- [ ] Play button → open video player
- [ ] Video served from local file path (for local dev)

### 3.6 Live Participant Count (Frontend)

- [ ] Show "👥 24 students" in header during class
- [ ] Update in real-time using LiveKit `useParticipants()` hook

---

## PHASE 4 — Production Hardening

### 4.1 Health Check Endpoint

- [ ] `GET /actuator/health` — Spring Boot Actuator
- [ ] Custom health indicator for LiveKit server connectivity
- [ ] Return DOWN if LiveKit server unreachable

### 4.2 API Documentation

- [ ] Add Swagger/OpenAPI (springdoc-openapi)
- [ ] Document all endpoints with request/response examples
- [ ] Available at `http://localhost:8084/swagger-ui.html`

### 4.3 Rate Limiting (Basic)

- [ ] Limit `/api/live/join` — max 10 requests per minute per IP
- [ ] Limit `/api/live/start` — max 5 requests per minute per instructorId
- [ ] Return 429 Too Many Requests with retry-after header

### 4.4 Frontend Error Handling (Per Status Code)

- [ ] 404 → "Class has not started yet. Please wait for instructor."
- [ ] 409 → "Class is full. Please contact your instructor."
- [ ] 503 → "Video server is temporarily down. Retrying in 3 seconds..." + auto retry
- [ ] 400 → Show field-level validation errors
- [ ] Network error → "No internet connection. Please check your network."
- [ ] Token expired → "Session expired. Please rejoin."

### 4.5 Retry Logic (Frontend)

- [ ] On 503 → auto retry join after 3 seconds (max 3 attempts)
- [ ] Show retry countdown to user ("Retrying in 3... 2... 1...")
- [ ] After 3 failed retries → show "Please try again later"

---

## PRODUCTION RULES — Always Follow

> These rules apply to EVERY piece of code written in this project.

### Backend Rules

```
1. NEVER use System.out.println — always use log.info/warn/error
2. NEVER throw generic RuntimeException — use custom exceptions
3. NEVER return stack trace to client — use ErrorResponse DTO
4. ALWAYS validate request body with @Valid
5. ALWAYS handle LiveKit call failures — wrap in try/catch
6. ALWAYS check idempotency in webhook handlers
7. ALWAYS rollback LiveKit changes if DB fails
8. ALWAYS add @Transactional where multiple DB operations happen
9. NEVER hardcode API keys — use application.yml + env variables
10. ALWAYS log: what happened + roomName + userId + timestamp
```

### Frontend Rules

```
1. NEVER store token in localStorage — keep in React state (memory) only
2. NEVER show raw error messages to user — show friendly messages
3. ALWAYS handle loading state — show spinner during API calls
4. ALWAYS handle network errors — not just HTTP errors
5. ALWAYS confirm before destructive actions (kick, end class)
6. ALWAYS show feedback after actions (toast notifications)
7. NEVER expose roomName in URL — keep in state
8. ALWAYS disable buttons during loading to prevent double-submit
```

### API Design Rules

```
1. Use proper HTTP status codes:
   200 → success
   400 → bad request (validation failed)
   401 → unauthorized (invalid token)
   403 → forbidden (not allowed)
   404 → not found
   409 → conflict (already exists / room full)
   500 → internal server error
   503 → service unavailable (LiveKit down)

2. Error response format — always consistent:
   { "errorCode": "SESSION_NOT_FOUND", "message": "...", "timestamp": "..." }

3. Never return null — return empty list [] or 404
4. Always return roomName in start session response
```

---

## PROGRESS SUMMARY

| Phase | Total Tasks | Done | Remaining |
|-------|------------|------|-----------|
| Already Built | 22 | 22 ✅ | 0 |
| Phase 1 — Foundation | 24 | 0 | 24 |
| Phase 2 — Core Features | 28 | 0 | 28 |
| Phase 3 — UX & Polish | 18 | 0 | 18 |
| Phase 4 — Production Hardening | 14 | 0 | 14 |
| Phase 5 — Recording Service | 0 | 0 | TBD |
| Phase 6 — Schedule Service | 0 | 0 | TBD |
| **Total** | **106+** | **22** | **84+** |

---

*Last Updated: May 18, 2026*
*Update this file after every completed task*

---

## PHASE 5 — lms-recording-service (New Microservice)

> **Port:** 8085
> **Responsibility:** Manage all recording operations via LiveKit Egress
> **Communicates with:** lms-live-service (WebClient calls), LiveKit Egress Docker

### 5.1 Project Setup

- [ ] Create new Spring Boot project `lms-recording-service` (port 8085)
- [ ] Add dependencies: spring-web, spring-data-jpa, h2, livekit-server SDK, lombok, validation
- [ ] `application.yml` — LiveKit keys, recordings folder path, lms-live-service URL
- [ ] `CorsConfig.java` — allow frontend origin
- [ ] `LiveKitEgressConfig.java` — EgressClient bean

### 5.2 DB Model

- [ ] `Recording.java` entity:
  ```
  id, instituteId, courseId, roomName, teacherId,
  egressId, filePath, fileName,
  status (STARTING / RECORDING / STOPPED / FAILED),
  startTime, stopTime, durationSeconds,
  fileSizeBytes, quality
  ```
- [ ] `RecordingRepository.java` — findByRoomName, findByCourseId, findByInstituteId

### 5.3 Recording Controller

- [ ] `POST /api/recording/start` — body: `{ roomName, courseId, instituteId, quality }`
- [ ] `POST /api/recording/stop` — body: `{ roomName }` or `{ egressId }`
- [ ] `GET /api/recording/list?courseId=xxx` — list recordings for a course
- [ ] `GET /api/recording/list?instituteId=xxx` — list all recordings for institute
- [ ] `GET /api/recording/{id}` — get single recording details
- [ ] `DELETE /api/recording/{id}` — delete recording file + DB record

### 5.4 Recording Service Logic

- [ ] `startRecording(roomName, courseId, quality)`:
  - Call EgressClient to start Room Composite Egress
  - Output: local file `/recordings/{instituteId}/{courseId}/{roomName}/{timestamp}.mp4`
  - Save egressId + status RECORDING to DB
  - If EgressClient fails → throw `RecordingFailedException` (class continues)
- [ ] `stopRecording(roomName)`:
  - Find active recording by roomName
  - Call EgressClient.stopEgress(egressId)
  - Update status to STOPPED in DB
  - If already stopped → log warning, return gracefully (idempotent)
- [ ] `handleEgressEnded(egressId, filePath, duration)`:
  - Called from webhook
  - Update filePath, durationSeconds, fileSizeBytes, status=STOPPED
- [ ] `handleEgressFailed(egressId, errorMessage)`:
  - Update status=FAILED, log error
  - Do NOT crash — recording failure is non-critical

### 5.5 Webhook Handler (Recording Service)

- [ ] `POST /api/recording/webhook` — receive LiveKit egress events
- [ ] Verify webhook signature (HMAC)
- [ ] Handle `egress_started` → update status to RECORDING
- [ ] Handle `egress_ended` → save filePath, duration, mark STOPPED
- [ ] Handle `egress_updated` → log progress
- [ ] Idempotency: check if already processed before updating

### 5.6 File Serving (Local Dev)

- [ ] `GET /api/recording/file/{id}` — serve MP4 file as stream
- [ ] Use Spring `Resource` to stream file from local path
- [ ] Support `Range` header for video seek (partial content 206)
- [ ] Return 404 if file not found on disk

### 5.7 Error Handling (Recording Service)

- [ ] `RecordingNotFoundException` → 404
- [ ] `RecordingAlreadyActiveException` → 409 (already recording this room)
- [ ] `RecordingFailedException` → 500
- [ ] `EgressServiceUnavailableException` → 503
- [ ] `GlobalExceptionHandler` with `ErrorResponse` DTO
- [ ] All exceptions logged with roomName + egressId

### 5.8 Integration — lms-live-service calls Recording Service

- [ ] Add `WebClient` bean in lms-live-service
- [ ] In webhook `room_started` → WebClient POST to recording-service `/api/recording/start`
- [ ] In webhook `room_finished` → WebClient POST to recording-service `/api/recording/stop`
- [ ] If recording-service is down → log error, do NOT fail the class (fire and forget)
- [ ] Timeout: 3 seconds max for WebClient call

### 5.9 Egress Docker Setup

- [ ] Document Docker command for Egress service
- [ ] Mount local `/recordings` folder as volume
- [ ] Egress connects to LiveKit server on `ws://localhost:7880`
- [ ] Verify egress service is running before starting any class

---

## PHASE 6 — lms-schedule-service (New Microservice)

> **Port:** 8086
> **Responsibility:** Weekly schedule management, slot booking, bandwidth reservation per institute
> **Communicates with:** lms-live-service (permission check before class start)

### 6.1 Project Setup

- [ ] Create new Spring Boot project `lms-schedule-service` (port 8086)
- [ ] Add dependencies: spring-web, spring-data-jpa, h2, lombok, validation
- [ ] `application.yml` — DB config, port
- [ ] `CorsConfig.java`

### 6.2 DB Models

- [ ] `InstitutePlan.java` entity:
  ```
  id, instituteId, planName (BASIC/PRO/ENTERPRISE),
  totalBandwidthMbps (e.g. 1000 for 1Gbps),
  maxStudentsTotal, maxConcurrentClasses,
  planStartDate, planEndDate, isActive
  ```

- [ ] `ScheduledSession.java` entity:
  ```
  id, instituteId, teacherId, teacherName,
  subject, courseId,
  startTime, endTime,
  studentCount, quality (540p/720p/360p),
  bandwidthReservedMbps (calculated at booking),
  status (SCHEDULED / ACTIVE / COMPLETED / CANCELLED),
  roomName (filled when class actually starts),
  createdAt, updatedAt
  ```

- [ ] `InstitutePlanRepository.java` — findByInstituteId
- [ ] `ScheduledSessionRepository.java`:
  - `findOverlappingSessions(instituteId, startTime, endTime)` — JPQL query
  - `findByInstituteIdAndWeek(instituteId, weekStart, weekEnd)`
  - `findByTeacherIdAndDate(teacherId, date)`

### 6.3 Bandwidth Calculation Logic

- [ ] `BandwidthCalculator.java` utility class:
  ```
  calculateBandwidth(studentCount, quality):
    360p  → studentCount × 0.5 Mbps
    540p  → studentCount × 1.0 Mbps
    720p  → studentCount × 2.5 Mbps
    1080p → studentCount × 4.0 Mbps
  ```
- [ ] `getReservedBandwidth(instituteId, startTime, endTime)`:
  - Find all SCHEDULED sessions overlapping this time slot
  - SUM their bandwidthReservedMbps
  - Return total reserved
- [ ] `getAvailableBandwidth(instituteId, startTime, endTime)`:
  - totalBandwidth (from plan) - reservedBandwidth
- [ ] `canBook(instituteId, startTime, endTime, studentCount, quality)`:
  - Calculate requested bandwidth
  - Check: reserved + requested <= total
  - Return: `{ canBook: true/false, availableMbps, requestedMbps, message }`

### 6.4 Schedule Controller

- [ ] `POST /api/schedule/book` — book a new slot:
  ```json
  {
    "instituteId": "inst-001",
    "teacherId": "teacher-001",
    "teacherName": "Prof. Sharma",
    "subject": "Physics",
    "courseId": "physics-101",
    "startTime": "2026-05-19T13:00:00",
    "endTime": "2026-05-19T14:00:00",
    "studentCount": 300,
    "quality": "540p"
  }
  ```
- [ ] `GET /api/schedule/week?instituteId=xxx&weekStart=2026-05-19` — weekly schedule view
- [ ] `GET /api/schedule/teacher?teacherId=xxx&date=2026-05-19` — teacher's day schedule
- [ ] `DELETE /api/schedule/{id}` — cancel a booking
- [ ] `GET /api/schedule/capacity?instituteId=xxx&startTime=xxx&endTime=xxx` — check available bandwidth for a slot
- [ ] `PATCH /api/schedule/{id}/activate` — called by lms-live-service when class actually starts
- [ ] `PATCH /api/schedule/{id}/complete` — called by lms-live-service when class ends

### 6.5 Booking Logic — Full Flow

- [ ] `bookSlot(request)`:
  1. Validate request (times, studentCount > 0, endTime > startTime)
  2. Check institute plan exists and is active
  3. Check slot duration max 4 hours
  4. Find overlapping sessions for same institute
  5. Calculate reserved bandwidth for that time slot
  6. Calculate requested bandwidth (studentCount × mbpsPerStudent)
  7. If reserved + requested > totalBandwidth → throw `InsufficientBandwidthException`
  8. If studentCount > plan.maxStudentsTotal → throw `StudentLimitExceededException`
  9. Save ScheduledSession with bandwidthReservedMbps
  10. Return booking confirmation with slot details

- [ ] Error message must be helpful:
  ```
  "Insufficient bandwidth for this time slot.
   Available: 700 Mbps, Required: 1000 Mbps.
   You can book max 700 students at 540p or 280 students at 720p
   for this time slot."
  ```

### 6.6 Permission Check — lms-live-service Integration

- [ ] `GET /api/schedule/permission?instituteId=xxx&courseId=xxx&teacherId=xxx` — called by lms-live-service before creating LiveKit room
- [ ] Returns: `{ allowed: true/false, scheduleId, reason }`
- [ ] lms-live-service checks this BEFORE creating room:
  - If `allowed: false` → return 403 to instructor with reason
  - If `allowed: true` → proceed to create LiveKit room
- [ ] After room created → call `PATCH /api/schedule/{id}/activate`
- [ ] After room ended → call `PATCH /api/schedule/{id}/complete`

### 6.7 Exception Handling (Schedule Service)

- [ ] `InsufficientBandwidthException` → 409 with available capacity details
- [ ] `StudentLimitExceededException` → 409 with plan limit details
- [ ] `SlotConflictException` → 409 (teacher already has class at same time)
- [ ] `PlanNotFoundException` → 404 (institute has no active plan)
- [ ] `PlanExpiredException` → 402 (plan expired, needs renewal)
- [ ] `InvalidTimeSlotException` → 400 (end before start, past time, etc.)
- [ ] `GlobalExceptionHandler` with consistent `ErrorResponse` DTO

### 6.8 Weekly Schedule View Logic

- [ ] Return 7-day schedule for an institute
- [ ] Group sessions by day
- [ ] Show for each session: subject, teacher, time, studentCount, status, bandwidthUsed
- [ ] Show daily bandwidth usage summary:
  ```
  Monday: 3 classes, 850 Mbps reserved out of 1000 Mbps (85% used)
  ```
- [ ] Highlight time slots where bandwidth > 80% (warning)
- [ ] Highlight time slots where bandwidth = 100% (full — no more bookings)

### 6.9 Frontend — Schedule Pages

- [ ] `WeeklySchedulePage.jsx` — calendar/grid view of weekly schedule
- [ ] `BookSlotPage.jsx` — form to book a new class slot
- [ ] Show bandwidth availability indicator per time slot (green/yellow/red)
- [ ] Show error message with available capacity when booking fails
- [ ] `CapacityBar.jsx` — visual bandwidth usage bar per time slot
- [ ] Teacher view — show only their own classes
- [ ] Institute admin view — show all classes across all teachers

### 6.10 Production Rules for Schedule Service

- [ ] All booking operations must be `@Transactional`
- [ ] Concurrent booking protection — use DB-level locking or optimistic locking
- [ ] Two teachers booking same slot simultaneously → only one should succeed
- [ ] Use `@Version` on ScheduledSession for optimistic locking
- [ ] All bandwidth calculations must use exact same formula everywhere
- [ ] Log every booking attempt with instituteId + teacherId + time + result

---

## UPDATED PROGRESS SUMMARY

| Phase | Total Tasks | Done | Remaining |
|-------|------------|------|-----------|
| Already Built | 22 | 22 ✅ | 0 |
| Phase 1 — Foundation (lms-live-service) | 24 | 0 | 24 |
| Phase 2 — Core Features (lms-live-service + Frontend) | 28 | 0 | 28 |
| Phase 3 — UX & Polish | 18 | 0 | 18 |
| Phase 4 — Production Hardening | 14 | 0 | 14 |
| Phase 5 — lms-recording-service | 35 | 0 | 35 |
| Phase 6 — lms-schedule-service | 42 | 0 | 42 |
| **Total** | **183** | **22** | **161** |

---

## SERVICE COMMUNICATION MAP

```
LiveKit-Frontend (3000)
        |
        | HTTP REST
        v
lms-live-service (8084)
        |
        |-- WebClient --> lms-recording-service (8085)  [start/stop recording]
        |-- WebClient --> lms-schedule-service (8086)   [permission check before class]
        |
        | LiveKit Server API (HTTP)
        v
LiveKit Server (7880)
        |
        | Webhooks (HTTP POST)
        v
lms-live-service (8084) /api/live/webhook
lms-recording-service (8085) /api/recording/webhook

LiveKit Egress (Docker)
        |
        | Saves files to
        v
/recordings/ folder (local disk)
        |
        | Served by
        v
lms-recording-service GET /api/recording/file/{id}
```

---

## BUILD ORDER (Recommended)

```
Step 1:  Phase 1 — Foundation in lms-live-service     (1 day)
Step 2:  Phase 2.1 — Room name display (Frontend)     (30 min)
Step 3:  Phase 5 — lms-recording-service              (2 days)
Step 4:  Phase 6 — lms-schedule-service               (3 days)
Step 5:  Phase 2.3 — Chat                             (1 day)
Step 6:  Phase 2.4/2.5 — Participant list + Mute/Kick (1 day)
Step 7:  Phase 3 — UX & Polish                        (2 days)
Step 8:  Phase 4 — Production Hardening               (1 day)
```

---

*Last Updated: May 18, 2026*
*Update this file after every completed task*

---

## PHASE 7 — Security, Hardening & Missing Critical Items

> **This is the LAST phase — but Critical items (🔴) must be done before production deploy**
> Priority: 🔴 Critical → 🟡 Important → 🟢 Good to Have

---

### 7.1 🔴 Security — Instructor & Student Verification

- [ ] `startSession()` — verify instructorId is not empty + matches a valid format
- [ ] `joinSession()` — verify token identity matches studentId in request body
- [ ] Mute API — validate that caller's identity has `roomAdmin` permission before allowing mute
- [ ] Kick API — validate that caller's identity has `roomAdmin` permission before allowing kick
- [ ] Reject any join request where studentId contains special characters or SQL patterns
- [ ] Service-to-service calls (live → recording, live → schedule) — add shared secret header
  - `X-Internal-Secret: <shared-secret>` in every WebClient call
  - Recording service + Schedule service verify this header before processing

### 7.2 🔴 Webhook Security — IP Whitelist

- [ ] LiveKit webhooks should only be accepted from LiveKit server IP
- [ ] Add IP whitelist check in `LiveKitWebhookController` before processing
- [ ] If IP not in whitelist → return 403 + log warning with caller IP
- [ ] For local dev: whitelist `127.0.0.1` and `localhost`
- [ ] Document allowed IPs in `application.yml`

### 7.3 🔴 Concurrent Session Prevention

- [ ] In `startSession()` — check if instructor already has an ACTIVE session:
  ```
  findByInstructorIdAndStatus(instructorId, ACTIVE) → if exists → throw SessionAlreadyActiveException
  ```
- [ ] Error message: "You already have an active class running. Please end it before starting a new one."
- [ ] In schedule service — check if teacher already has a class at same time slot → `SlotConflictException`
- [ ] Log every conflict attempt with teacherId + existing roomName

### 7.4 🔴 Database Indexes

- [ ] `live_sessions` table:
  - Index on `room_name` (most queried column)
  - Index on `instructor_id + status` (concurrent session check)
  - Index on `course_id`
- [ ] `scheduled_sessions` table:
  - Composite index on `institute_id + start_time + end_time` (overlap query)
  - Index on `teacher_id + start_time`
  - Index on `status`
- [ ] `recordings` table:
  - Index on `room_name`
  - Index on `course_id`
  - Index on `egress_id`
- [ ] `attendance` table:
  - Index on `room_name`
  - Index on `participant_identity + room_name`
- [ ] Add indexes via `@Index` annotation on entity classes

### 7.5 🔴 Session Duration Limit + Auto-End

- [ ] Add `scheduledEndTime` field to `LiveSession` entity
- [ ] When class starts → set `scheduledEndTime = startTime + scheduledDuration`
- [ ] Spring `@Scheduled` task — every minute check for sessions past their end time
- [ ] If `now() > scheduledEndTime` → auto-call `endSession(roomName)`
- [ ] Notify instructor 5 minutes before auto-end: "Your class will auto-end in 5 minutes"
- [ ] Schedule service: `maxDuration = endTime - startTime` → pass to live service at class start
- [ ] Log every auto-end with roomName + reason

### 7.6 🟡 Pagination on All List APIs

- [ ] `GET /api/recording/list` — add `?page=0&size=20&sort=recordedAt,desc`
- [ ] `GET /api/schedule/week` — paginate if institute has many sessions
- [ ] `GET /api/live/attendance` — add `?page=0&size=50`
- [ ] Use Spring `Pageable` + `Page<T>` return type
- [ ] Response format:
  ```json
  {
    "content": [...],
    "totalElements": 150,
    "totalPages": 8,
    "currentPage": 0,
    "pageSize": 20
  }
  ```
- [ ] Never return unbounded list — always paginate

### 7.7 🟡 Docker Compose — One Command Setup

- [ ] Create `docker-compose.yml` at project root:
  - LiveKit Server container
  - LiveKit Egress container
  - lms-live-service container
  - lms-recording-service container
  - lms-schedule-service container
- [ ] Create `.env` file — all keys in one place:
  ```
  LIVEKIT_API_KEY=devkey
  LIVEKIT_API_SECRET=devsecret
  RECORDINGS_PATH=./recordings
  INTERNAL_SECRET=internal-secret-key
  ```
- [ ] Create `.env.example` — template for new developers (no real values)
- [ ] Service startup order: LiveKit first → then Spring Boot services
- [ ] `healthcheck` on LiveKit container — Spring services wait until LiveKit is ready
- [ ] Document: "Run `docker-compose up` to start everything locally"

### 7.8 🟡 Request/Response Audit Logging

- [ ] Create `AuditLogFilter.java` — Spring `OncePerRequestFilter`
- [ ] Log every incoming request: `method + endpoint + userId + IP + timestamp`
- [ ] Log every response: `status code + response time (ms)`
- [ ] Skip logging for: `/actuator/health`, `/h2-console`
- [ ] Format:
  ```
  [REQUEST]  POST /api/v1/live/start | userId=instructor-001 | IP=192.168.1.1
  [RESPONSE] POST /api/v1/live/start | status=200 | time=145ms
  ```
- [ ] Apply to all 3 microservices

### 7.9 🟡 Frontend — Reconnection Handling

- [ ] Handle `onReconnecting` event — show "Reconnecting..." overlay
- [ ] Handle `onReconnected` event — hide overlay, restore UI state
- [ ] Handle `onDisconnected` with reason:
  - `KICKED` → "You were removed from the class by the instructor"
  - `ROOM_DELETED` → "The class has ended"
  - `NETWORK_ERROR` → "Connection lost. Attempting to reconnect..."
  - `TOKEN_EXPIRED` → "Session expired. Please rejoin the class."
- [ ] Preserve chat messages during reconnection (messages in state, not lost)
- [ ] Preserve raise hand state during reconnection
- [ ] Show connection quality indicator (good/poor/disconnected)

### 7.10 🟡 Environment Config — Proper Setup

- [ ] `application.yml` — replace hardcoded keys with env variables + fallback:
  ```yaml
  livekit:
    api:
      key: ${LIVEKIT_API_KEY:devkey}
      secret: ${LIVEKIT_API_SECRET:devsecret}
  ```
- [ ] Frontend `.env` file:
  ```
  VITE_BACKEND_URL=http://localhost:8084
  VITE_LIVEKIT_URL=ws://localhost:7880
  ```
- [ ] Create `.env.example` for frontend and each backend service
- [ ] NEVER commit `.env` files to git — add to `.gitignore`
- [ ] Document all environment variables in README

### 7.11 🟡 API Versioning

- [ ] Change all endpoints from `/api/live/` to `/api/v1/live/`
- [ ] Change all endpoints from `/api/recording/` to `/api/v1/recording/`
- [ ] Change all endpoints from `/api/schedule/` to `/api/v1/schedule/`
- [ ] Update frontend `liveApi.js` to use `/api/v1/` prefix
- [ ] Update WebClient calls between services to use `/api/v1/` prefix

### 7.12 🟡 Input Sanitization

- [ ] Strip HTML/script tags from all string inputs (roomName, teacherName, subject)
- [ ] Trim whitespace from all string fields before saving to DB
- [ ] Normalize roomName — lowercase, replace spaces with dashes
- [ ] Max length enforcement on all string fields at DB level (`@Column(length=100)`)
- [ ] Reject null bytes and control characters in any input field

### 7.13 🟢 Circuit Breaker — Resilience4j

- [ ] Add `resilience4j-spring-boot3` dependency to lms-live-service
- [ ] Circuit breaker on WebClient call to recording-service:
  - After 3 failures → open circuit → skip recording (class continues)
  - After 30 seconds → half-open → try again
- [ ] Circuit breaker on WebClient call to schedule-service:
  - After 3 failures → open circuit → allow class with warning log
- [ ] Fallback method for each circuit breaker
- [ ] Expose circuit breaker status via `/actuator/health`

### 7.14 🟢 Recurring Schedule

- [ ] Add `recurrenceType` field to `ScheduledSession`: `ONCE / DAILY / WEEKLY`
- [ ] Add `recurrenceEndDate` field — when to stop generating sessions
- [ ] Add `parentScheduleId` — link recurring sessions to original booking
- [ ] `generateRecurringSessions(booking)`:
  - WEEKLY → generate sessions for every same weekday until recurrenceEndDate
  - Check bandwidth for EACH generated session before saving
  - If any slot fails bandwidth check → report which dates failed
- [ ] `DELETE /api/v1/schedule/{id}?deleteAll=true` — cancel all future recurring sessions
- [ ] `DELETE /api/v1/schedule/{id}?deleteAll=false` — cancel only this one session

### 7.15 🟢 Notification System (Basic)

- [ ] `NotificationService.java` in lms-live-service (simple, no separate microservice yet)
- [ ] In-memory notification store (list per userId) — for local dev
- [ ] `GET /api/v1/notifications?userId=xxx` — poll for new notifications
- [ ] Trigger notifications:
  - Class starts → notify enrolled students: "Your Physics class is starting now"
  - Recording ready → notify students: "Recording is now available"
  - Class auto-ending in 5 min → notify instructor
  - Schedule booked → confirmation to teacher
- [ ] Mark notification as read: `PATCH /api/v1/notifications/{id}/read`
- [ ] Frontend: poll every 30 seconds for new notifications
- [ ] Show notification bell icon with unread count in UI

### 7.16 🟢 README & Developer Documentation

- [ ] `README.md` at project root:
  - Project overview + architecture
  - Prerequisites (Java 17, Node 18, Docker)
  - How to run locally step by step
  - All service URLs table
  - Environment variables list
  - How to run with Docker Compose
- [ ] Swagger URLs for each service
- [ ] Troubleshooting section (common errors + fixes)

---

## FINAL PROGRESS SUMMARY

| Phase | Total Tasks | Done | Remaining |
|-------|------------|------|-----------|
| Already Built | 22 | 22 ✅ | 0 |
| Phase 1 — Foundation (lms-live-service) | 24 | 0 | 24 |
| Phase 2 — Core Features | 28 | 0 | 28 |
| Phase 3 — UX & Polish | 18 | 0 | 18 |
| Phase 4 — Production Hardening | 14 | 0 | 14 |
| Phase 5 — lms-recording-service | 35 | 0 | 35 |
| Phase 6 — lms-schedule-service | 42 | 0 | 42 |
| Phase 7 — Security, Hardening & Missing Items | 58 | 0 | 58 |
| **TOTAL** | **241** | **22** | **219** |

---

## 🚨 CRITICAL ITEMS — Must Complete Before Any Demo

```
From Phase 7 — do these FIRST even before Phase 2/3/4:

7.3  Concurrent session prevention     ← instructor can start 2 classes right now
7.4  Database indexes                  ← queries will be slow without this
7.1  Security verification             ← anyone can call APIs right now
7.5  Session duration limit            ← class can run forever right now
7.2  Webhook IP whitelist              ← fake webhooks possible right now
```

---

*Last Updated: May 18, 2026*
*Update this file after every completed task*

---

## PHASE 8 — Edge Cases & System Reliability

> These are real-world scenarios that will break the system in production if not handled.

---

### 8.1 🔴 Institute Data Isolation (Point 10)

- [ ] Every query in schedule-service must include `instituteId` filter — never return cross-institute data
- [ ] Every query in recording-service must include `instituteId` filter
- [ ] Every query in live-service must include `instituteId` filter where applicable
- [ ] Add `instituteId` to `LiveSession` entity if not already present
- [ ] Validate in every API: `instituteId` in request must match `instituteId` of the resource being accessed
- [ ] If mismatch → return 403 Forbidden (not 404 — don't reveal other institute's data exists)
- [ ] Write test: Institute A teacher cannot access Institute B's schedule/recordings

### 8.2 🔴 Bandwidth Race Condition — Concurrent Booking (Point 8)

- [ ] Two teachers from same institute booking same time slot simultaneously → only one should succeed
- [ ] Current `@Version` optimistic locking on `ScheduledSession` is not enough — need to lock at bandwidth check level
- [ ] Use `SELECT ... FOR UPDATE` (pessimistic locking) on `InstitutePlan` row during booking transaction:
  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM InstitutePlan p WHERE p.instituteId = :id")
  Optional<InstitutePlan> findByInstituteIdForUpdate(@Param("id") String id);
  ```
- [ ] Wrap entire `bookSlot()` in `@Transactional` with this lock
- [ ] Second concurrent request will wait → then re-check bandwidth → fail if no longer available
- [ ] Test: simulate 2 simultaneous booking requests for same slot → verify only 1 succeeds

### 8.3 🟡 Health Check for All 3 Services (Point 10)

- [ ] Add Spring Boot Actuator to `lms-recording-service`
- [ ] Add Spring Boot Actuator to `lms-schedule-service`
- [ ] Custom health indicator in `lms-live-service` — check if recording-service is reachable
- [ ] Custom health indicator in `lms-live-service` — check if schedule-service is reachable
- [ ] Custom health indicator in `lms-live-service` — check if LiveKit server is reachable
- [ ] Custom health indicator in `lms-recording-service` — check if Egress Docker is reachable
- [ ] All health endpoints: `GET /actuator/health` on each service
- [ ] Response format:
  ```json
  {
    "status": "UP",
    "components": {
      "livekit": { "status": "UP" },
      "recordingService": { "status": "UP" },
      "scheduleService": { "status": "DOWN", "reason": "Connection refused" }
    }
  }
  ```

### 8.4 🟡 Graceful Shutdown (Point 11)

- [ ] Add `ApplicationListener<ContextClosedEvent>` in `lms-live-service`
- [ ] On shutdown → find all sessions with status `ACTIVE` in DB
- [ ] Mark them as `INTERRUPTED` (add new status to `SessionStatus` enum)
- [ ] Log: "Service shutting down — marking X active sessions as INTERRUPTED"
- [ ] Add `INTERRUPTED` status handling in frontend — show "Class was interrupted. Please rejoin when available."
- [ ] On service restart → check for INTERRUPTED sessions → instructor can resume or start fresh
- [ ] Same for `lms-recording-service` — on shutdown → stop any active egress gracefully before exit

### 8.5 🟡 Recording Disk Space Management (Point 3)

- [ ] Add `diskSpaceMonitor()` scheduled task in `lms-recording-service` — runs every hour
- [ ] Check available disk space on recordings folder
- [ ] If disk < 20% free → log warning + send notification to platform admin
- [ ] If disk < 5% free → block new recordings + return 507 Insufficient Storage
- [ ] Add `GET /api/v1/recording/storage-stats` endpoint:
  ```json
  {
    "totalSpaceGB": 500,
    "usedSpaceGB": 420,
    "freeSpaceGB": 80,
    "usedPercent": 84,
    "status": "WARNING"
  }
  ```
- [ ] Add max storage limit per institute in `InstitutePlan` (e.g. 50 GB per institute)
- [ ] Before starting new recording → check if institute has exceeded storage quota
- [ ] If quota exceeded → block recording + notify institute admin

### 8.6 🟡 Class Joining Time Window (Point 4)

- [ ] Students should only be able to join within a valid time window
- [ ] Allow join: `scheduledStartTime - 15 minutes` to `scheduledEndTime`
- [ ] Before issuing student token → check current time against schedule:
  - Too early (> 15 min before start) → 403: "Class starts at {time}. You can join 15 minutes before."
  - Too late (after end time) → 403: "This class has ended. Recording will be available soon."
- [ ] Add `GET /api/v1/schedule/join-window?courseId=xxx` — returns next valid join window
- [ ] Instructor is exempt from time window — can start class anytime on scheduled day
- [ ] Handle edge case: unscheduled classes (no schedule entry) → allow join anytime room is ACTIVE

### 8.7 🟡 Time Zone Handling (Point 12)

- [ ] All `startTime` / `endTime` stored in DB as UTC
- [ ] `ScheduledSession` entity — add `instituteTimezone` field (e.g. `"Asia/Kolkata"`)
- [ ] `InstitutePlan` entity — add `timezone` field
- [ ] All API responses — return times in both UTC and institute local time:
  ```json
  {
    "startTimeUtc": "2026-05-19T07:30:00Z",
    "startTimeLocal": "2026-05-19T13:00:00+05:30",
    "timezone": "Asia/Kolkata"
  }
  ```
- [ ] Booking API — accept time in institute's local timezone, convert to UTC before saving
- [ ] Weekly schedule view — display times in institute's local timezone
- [ ] Auto-end scheduler — compare `now()` in UTC against `scheduledEndTime` in UTC
- [ ] Frontend — display times in user's browser timezone using `Intl.DateTimeFormat`

---

## FINAL UPDATED PROGRESS SUMMARY

| Phase | Total Tasks | Done | Remaining |
|-------|------------|------|-----------|
| Already Built | 22 | 22 ✅ | 0 |
| Phase 1 — Foundation | 24 | 0 | 24 |
| Phase 2 — Core Features | 28 | 0 | 28 |
| Phase 3 — UX & Polish | 18 | 0 | 18 |
| Phase 4 — Production Hardening | 14 | 0 | 14 |
| Phase 5 — lms-recording-service | 35 | 0 | 35 |
| Phase 6 — lms-schedule-service | 42 | 0 | 42 |
| Phase 7 — Security & Hardening | 58 | 0 | 58 |
| Phase 8 — Edge Cases & Reliability | 38 | 0 | 38 |
| **TOTAL** | **279** | **22** | **257** |

---

*Last Updated: May 18, 2026*
*Update this file after every completed task*
