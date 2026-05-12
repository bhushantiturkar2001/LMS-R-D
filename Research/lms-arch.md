# Unified LMS - Complete Architecture Document
**Project:** Unified LMS Platform
**Tech Stack:** React + React Native + Java Spring Boot Microservices
**Researcher:** Bhushan
**Date:** May 11, 2026

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Layer 1 - Frontend (Client Layer)](#layer-1---frontend-client-layer)
3. [Layer 2 - API Gateway](#layer-2---api-gateway)
4. [Layer 3 - Users & Roles](#layer-3---users--roles)
5. [Layer 4 - Microservices](#layer-4---microservices)
6. [Sync vs Async Communication](#sync-vs-async-communication)
7. [LiveKit Integration - Sync + Async](#livekit-integration---sync--async)
8. [Load Balancer & LiveKit SFU](#load-balancer--livekit-sfu)
9. [Database & LLMs](#database--llms)
10. [Complete Flow Summary](#complete-flow-summary)
11. [Key Decision Table](#key-decision-table)

---

## Architecture Overview

```
                        Unified LMS
                            |
          ┌─────────────────┴─────────────────┐
          ↓                                   ↓
   WEB UI (React)                  Mobile APP (React Native)
      ALL Users                       Students Only
          │                                   │
          └──────────────┬────────────────────┘
                         ↓
          ┌──────────────────────────────┐
          │  API Gateway                 │
          │  (RBAC, JWT Token, Security) │
          └──────────────────────────────┘
                         │
          ┌──────────────┴──────────────┐
          ↓                             ↓
 Domain Specific Service         Common Service
 ├── Student                     ├── Cart & Payment
 ├── Instructor                  ├── Chat
 ├── Institute                   ├── Report
 ├── Platform Admin              ├── Live Session
 └── Recommendation (AI)         ├── Email
                                 ├── Addon
                                 ├── SMS
                                 ├── Auth
                                 ├── LS Monitoring
                                 ├── Audit
                                 ├── Finance
                                 ├── Ticket & Support
                                 ├── Log Collector
                                 └── AI Course Generation
                         │
          ┌──────────────┴──────────────┐
          ↓              ↓              ↓
   Load Balancer      Database        LLMs
        ↓
  LiveKit SFU Server
  1Gbps : 540p × 500
          720p × 400
```

---

## Layer 1 - Frontend (Client Layer)

### Two Clients

| Client | Technology | Target Users |
|--------|-----------|--------------|
| Web UI | React | Students, Instructors, Institute, Admins - **ALL** |
| Mobile App | React Native | **Students Only** |

### Why React Native Only for Students?
- Students mobile pe zyada padhte hain
- Instructors/Admins ko complex dashboards chahiye → Web better hai
- Cost saving → Sirf ek mobile app maintain karo
- Admin operations (bulk upload, analytics) mobile pe practical nahi

---

## Layer 2 - API Gateway

### Responsibilities
```
API Gateway
├── RBAC  → Role Based Access Control (kaun kya access kar sakta hai)
├── JWT   → Token validation (har request pe)
└── Security → Rate Limiting, DDoS Protection, SSL Termination
```

### Spring Cloud Gateway Implementation

```java
// API Gateway - Spring Cloud Gateway
@Component
public class JwtAuthFilter implements GlobalFilter {

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest()
                               .getHeaders()
                               .getFirst("Authorization");

        // Step 1: JWT validate karo
        // Step 2: Role check karo (RBAC)
        // Step 3: Agar valid → downstream service ko forward karo
        // Step 4: Agar invalid → 401 Unauthorized return karo
    }
}
```

### RBAC - Role Based Access Control

| Role | Access Level |
|------|-------------|
| Student | Courses view/enroll, Live class join, Recordings watch |
| Private Instructor | Course create/manage, Live class host |
| Institute Super Admin | Full institute control, Branch management |
| Institute Branch Admin | Branch students & tutors manage |
| Institute Tutor | Classes lena, Assignments grade karna |
| Platform Super Admin | Poora platform control |
| Platform Sub Admin | Limited platform operations |

---

## Layer 3 - Users & Roles

```
Users (5 Types)
│
├── 1. Students
│       └── Course enroll, Live class join, Assignments submit
│
├── 2. Private Instructors (Independent)
│       └── Freelance teachers (Udemy style)
│
├── 3. Institute
│   ├── 3.1 Super Admin  → Institute owner (FIITJEE, Allen jaise)
│   ├── 3.2 Branch Admin → Delhi/Mumbai branch manager
│   └── 3.3 Tutors       → Teachers jo class lete hain
│
├── 4. Platform Super Admin
│       └── Tumhari company ka CEO/CTO level access
│
└── 5. Platform Sub Admin
        └── Support team, limited platform control
```

### Real World Mapping

| User Type | Real World Example |
|-----------|-------------------|
| Student | Jo padh raha hai |
| Private Instructor | YouTube educator jo courses bechta hai |
| Institute Super Admin | Allen Career Institute HQ |
| Branch Admin | Allen Kota Branch Manager |
| Tutor | Physics teacher at Allen |
| Platform Super Admin | Your company's platform owner |
| Platform Sub Admin | Customer support / operations team |

---

## Layer 4 - Microservices

### 4A. Domain Specific Services

```
Domain Specific Service
├── Student Service       → Profile, enrollment, progress tracking
├── Instructor Service    → Profile, course ownership, earnings
├── Institute Service     → Institute setup, branch management
├── Platform Admin Service→ Platform-wide settings, user management
└── Recommendation (AI)  → "Yeh course tumhare liye best hai"
```

#### Student Service Example (Spring Boot)

```java
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @GetMapping("/{id}/courses")
    public ResponseEntity<List<Course>> getEnrolledCourses(@PathVariable Long id) {
        // Student ke enrolled courses return karo
        return ResponseEntity.ok(studentService.getEnrolledCourses(id));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<Void> enrollCourse(
            @PathVariable Long id,
            @RequestBody EnrollRequest req) {

        // SYNC: DB mein enroll karo
        enrollmentService.enroll(id, req.getCourseId());

        // ASYNC: Kafka pe event publish karo (notification ke liye)
        kafkaTemplate.send("enrollment-events",
            new EnrollmentEvent(id, req.getCourseId(), "ENROLLED"));

        return ResponseEntity.ok().build();
    }
}
```

---

### 4B. Common Services (Shared Across All Users)

```
Common Service
├── Cart & Payment      → Purchase flow, payment gateway integration
├── Chat                → Real-time messaging (instructor ↔ student)
├── Report              → Analytics, performance reports
├── Live Session        → LiveKit integration (MAIN SERVICE)
├── Email               → Transactional emails
├── Addon               → Extra features/plugins
├── SMS                 → OTP, notifications via SMS
├── Auth                → Login, signup, OAuth, OTP
├── LS Monitoring       → Live Session health monitoring
├── Audit               → Who did what, when (compliance)
├── Finance             → Revenue, payouts, invoices
├── Ticket & Support    → Help desk, issue tracking
├── Log Collector       → Centralized logging (ELK Stack)
└── AI Course Generation→ AI se course content generate karo
```

**Why Common Services?**
- Student ho ya Instructor → Same payment service use hogi
- Sabko email chahiye → Ek email service sab use karein
- Code duplication avoid hota hai
- Independent scaling possible hai

---

## Sync vs Async Communication

### SYNC (Synchronous) - Turant Jawab Chahiye

```
User Request ──→ Service ──→ Response
                 (User WAIT karta hai)
```

**Use karo jab:**
- User ko turant response chahiye
- Data critical hai (payment, auth)
- Error handle karna zaroori hai immediately

#### SYNC Use Cases in LMS

| Scenario | Why SYNC |
|----------|----------|
| Login → JWT Token | Turant token chahiye, bina iske aage nahi badh sakte |
| Course Enroll → Confirm/Reject | User ko pata hona chahiye enrolled hua ya nahi |
| Payment → Success/Fail | Money involved, turant confirm karo |
| LiveKit Token Generate | Join karne se pehle token chahiye |
| Student Profile Fetch | Page load pe turant data chahiye |
| Room Capacity Check | Join karne se pehle check karo |

#### SYNC Code Example

```java
// SYNC - LiveKit Token Generation
// User yahan WAIT karta hai - isliye SYNC
@PostMapping("/api/rooms/join")
public ResponseEntity<TokenResponse> joinRoom(@RequestBody JoinRequest req) {

    // Check 1: Student enrolled hai? (SYNC)
    boolean enrolled = studentService.isEnrolled(
        req.getStudentId(), req.getCourseId());
    if (!enrolled) {
        return ResponseEntity.status(403).body("Not enrolled");
    }

    // Check 2: Room active hai? (SYNC)
    boolean roomActive = liveSessionService.isRoomActive(req.getRoomId());
    if (!roomActive) {
        return ResponseEntity.status(404).body("Class not started yet");
    }

    // Token generate karo (SYNC - turant chahiye)
    String token = liveKitTokenService.generate(
        req.getRoomId(), req.getUserId(), req.getRole());

    return ResponseEntity.ok(new TokenResponse(token, liveKitServerUrl));
    // Frontend yahan WAIT karta hai, token milne ke baad join karta hai
}
```

---

### ASYNC (Asynchronous) - Background Mein Hoga

```
User Request ──→ Service ──→ "OK noted" (turant response)
                                  │
                             Kafka Topic
                                  │
                    ┌─────────────┼─────────────┐
                    ↓             ↓             ↓
             Email Service   SMS Service   Audit Service
             (background)    (background)  (background)
```

**Use karo jab:**
- User ko wait nahi karwana
- Notification/Email/SMS bhejni hai
- Heavy processing hai (video transcoding)
- Multiple services ko inform karna hai (fan-out)

#### ASYNC Use Cases in LMS

| Scenario | Why ASYNC |
|----------|-----------|
| Enrollment → Email confirmation | User wait nahi karega email ke liye |
| Live class start → 500 students notify | Bulk operation, background mein |
| Payment done → Receipt email + Finance update | Non-blocking |
| Assignment submit → Instructor notify | Background notification |
| LiveKit Recording done → Process + Save | Heavy task, time lagta hai |
| Audit logs | Non-critical, background mein save karo |

#### ASYNC Code Example - Kafka

```java
// ASYNC - Enrollment Notification via Kafka
@PostMapping("/api/courses/enroll")
public ResponseEntity<String> enrollCourse(@RequestBody EnrollRequest req) {

    // SYNC part - DB mein enroll karo (critical)
    enrollmentService.enroll(req.getStudentId(), req.getCourseId());

    // ASYNC part - Kafka pe event publish karo
    // Student WAIT nahi karega notification ke liye
    kafkaTemplate.send("enrollment-events", new EnrollmentEvent(
        req.getStudentId(),
        req.getCourseId(),
        "ENROLLED",
        LocalDateTime.now()
    ));

    // Turant response - email baad mein aayegi
    return ResponseEntity.ok("Enrolled successfully");
}

// ---- Notification Service (Separate Microservice) ----
@KafkaListener(topics = "enrollment-events", groupId = "notification-group")
public void handleEnrollment(EnrollmentEvent event) {
    // Email bhejo
    emailService.sendEnrollmentConfirmation(event.getStudentId());
    // SMS bhejo
    smsService.sendSMS(event.getStudentId(), "Course enrolled successfully!");
    // Push notification
    pushService.send(event.getStudentId(), "Welcome to the course!");
}

// ---- Audit Service (Separate Microservice) ----
@KafkaListener(topics = "enrollment-events", groupId = "audit-group")
public void auditEnrollment(EnrollmentEvent event) {
    // Audit log save karo
    auditRepository.save(new AuditLog("ENROLLMENT", event));
}
```

---

## LiveKit Integration - Sync + Async

### Complete LiveKit Flow

```
Student "Join Class" click karta hai
         │
         ▼
    [SYNC] Token generate karo ──→ LiveKit Server
         │                              │
         │◄─────── Token return ────────┘
         │
    Student joins LiveKit Room
         │
    ┌────┴────────────────────────────┐
    │     Class chalta hai...         │
    │     Video streaming             │
    │     Chat, Q&A                   │
    └────┬────────────────────────────┘
         │
    Class khatam (LiveKit Webhook fire hota hai)
         │
         ▼
    [ASYNC via Kafka]
    ├── Recording process karo
    ├── Attendance save karo
    ├── Students ko notify karo ("Recording available")
    ├── Finance update karo (instructor earnings)
    └── Audit log save karo
```

### Live Session Service - Full Implementation

```java
@RestController
@RequestMapping("/api/live")
public class LiveSessionController {

    // ─────────────────────────────────────────
    // SYNC - Join Room (turant token chahiye)
    // ─────────────────────────────────────────
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> joinLiveClass(@RequestBody JoinRequest req) {

        // Enrollment check (SYNC)
        if (!studentService.isEnrolled(req.getStudentId(), req.getCourseId())) {
            return ResponseEntity.status(403).build();
        }

        // Room capacity check (SYNC)
        if (!liveSessionService.hasCapacity(req.getRoomId())) {
            return ResponseEntity.status(409).body("Room is full");
        }

        // LiveKit token generate (SYNC - must have before joining)
        String token = liveKitTokenService.generateToken(
            req.getRoomId(),
            req.getUserId(),
            ParticipantRole.SUBSCRIBER
        );

        return ResponseEntity.ok(new JoinResponse(token, liveKitServerUrl));
    }

    // ─────────────────────────────────────────
    // ASYNC - LiveKit Webhook Handler
    // ─────────────────────────────────────────
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleLiveKitWebhook(@RequestBody WebhookEvent event) {

        switch (event.getType()) {

            case "room_finished":
                // ASYNC - Kafka pe bhejo, heavy processing background mein
                kafkaTemplate.send("live-session-events", new SessionEndEvent(
                    event.getRoomId(),
                    event.getRecordingUrl(),
                    event.getDuration()
                ));
                break;

            case "participant_joined":
                // ASYNC - Attendance track karo
                kafkaTemplate.send("attendance-events", new AttendanceEvent(
                    event.getRoomId(),
                    event.getParticipantId(),
                    "JOINED"
                ));
                break;
        }

        // LiveKit ko turant 200 OK return karo
        return ResponseEntity.ok().build();
    }
}

// ─────────────────────────────────────────────────────
// Recording Service - Kafka Consumer (ASYNC processing)
// ─────────────────────────────────────────────────────
@Service
public class RecordingProcessor {

    @KafkaListener(topics = "live-session-events", groupId = "recording-group")
    public void processRecording(SessionEndEvent event) {

        // Step 1: Recording download from LiveKit storage
        String rawRecording = liveKitStorage.download(event.getRecordingUrl());

        // Step 2: Transcode (heavy task - ASYNC mein sahi hai)
        String processedUrl = videoTranscoder.transcode(rawRecording);

        // Step 3: S3 pe upload karo
        String s3Url = s3Service.upload(processedUrl);

        // Step 4: DB mein metadata save karo
        recordingRepository.save(new Recording(
            event.getRoomId(), s3Url, event.getDuration()));

        // Step 5: Students ko notify karo (another ASYNC event)
        kafkaTemplate.send("notification-events", new NotificationEvent(
            event.getRoomId(),
            "Recording is now available",
            NotificationType.RECORDING_READY
        ));
    }
}
```

---

## Load Balancer & LiveKit SFU

### Configuration

```
Load Balancer (Nginx / AWS ALB)
        │
        ├── LiveKit Server Instance 1
        ├── LiveKit Server Instance 2
        └── LiveKit Server Instance 3

Each Instance:
├── Bandwidth: 1 Gbps
├── 540p quality → 500 concurrent students
└── 720p quality → 400 concurrent students
```

### Capacity Planning

| Quality | Concurrent Users | Bandwidth per User | Total Bandwidth |
|---------|-----------------|-------------------|-----------------|
| 540p | 500 | ~2 Mbps | ~1 Gbps |
| 720p | 400 | ~2.5 Mbps | ~1 Gbps |
| 1080p | 200 | ~5 Mbps | ~1 Gbps |

### Load Balancer Responsibilities
- Multiple LiveKit servers ke beech traffic distribute karna
- Health checks - agar ek server down ho → dusre pe route karo
- Auto-scaling support - peak time pe naye instances add karo
- Sticky sessions - ek student same server se connected rahe

---

## Database & LLMs

### Database Strategy

```
Database Layer
├── PostgreSQL    → Relational data (users, courses, enrollments, payments)
├── MongoDB       → Flexible data (course content, quiz questions)
├── Redis         → Cache + Session store + Real-time data
└── S3/MinIO      → Video storage, recordings, documents
```

### LLMs Integration

```
LLMs
├── AI Course Generation  → Common Service mein
│   └── Instructor prompt deta hai → AI course outline/content generate karta hai
│
├── Recommendation Engine → Domain Specific Service mein
│   └── Student history dekh ke → "Yeh course tumhare liye best hai"
│
└── AI Tutor/Chatbot      → Common Service mein
    └── Student ka doubt → AI 24/7 answer deta hai
```

---

## Complete Flow Summary

### Request Flow

```
React / React Native
        │
        ▼
API Gateway
├── JWT Token validate karo
├── RBAC check karo (role-based access)
└── Route to correct microservice
        │
        ├── SYNC calls ──→ Domain/Common Services ──→ DB ──→ Response
        │
        └── ASYNC events ──→ Kafka
                                ├── Notification Service (Email/SMS/Push)
                                ├── Recording Service (LiveKit recordings process)
                                ├── Audit Service (compliance logs)
                                └── Finance Service (payment records, payouts)

LiveKit SFU ←── Load Balancer (video traffic separately handled)
```

### Service Communication Patterns

```
┌─────────────────────────────────────────────────────────┐
│                    Communication Patterns                │
├─────────────────┬───────────────────────────────────────┤
│ REST (SYNC)     │ Frontend ↔ API Gateway ↔ Services     │
│ Kafka (ASYNC)   │ Service → Kafka → Service             │
│ WebSocket       │ Frontend ↔ LiveKit (video/audio)      │
│ WebSocket       │ Frontend ↔ Chat Service               │
│ Webhook         │ LiveKit → Live Session Service        │
└─────────────────┴───────────────────────────────────────┘
```

---

## Key Decision Table

| Scenario | Pattern | Technology | Reason |
|----------|---------|-----------|--------|
| Login / JWT | SYNC | REST | Turant token chahiye |
| LiveKit room join token | SYNC | REST | Video start karne se pehle |
| Enrollment confirmation email | ASYNC | Kafka | User wait nahi karega |
| Live class start → 500 students notify | ASYNC | Kafka | Bulk, non-blocking |
| Recording processing | ASYNC | Kafka | Heavy task, time lagta hai |
| Payment success/fail | SYNC | REST | Confirm/fail turant batao |
| Payment receipt email | ASYNC | Kafka | Email baad mein aa sakti hai |
| Audit logs | ASYNC | Kafka | Non-critical, background |
| Real-time chat | ASYNC | WebSocket | Bi-directional real-time |
| LiveKit webhook (class end) | ASYNC | Kafka | Background processing trigger |
| Student profile fetch | SYNC | REST | Page load pe turant |
| AI course generation | ASYNC | Kafka | Heavy LLM processing |

---

## Tech Stack Summary

```
Frontend
├── Web:    React + TypeScript + TailwindCSS
└── Mobile: React Native

Backend
├── Framework:    Java Spring Boot (Microservices)
├── API Gateway:  Spring Cloud Gateway
├── Service Mesh: (Optional) Istio / Spring Cloud
└── Auth:         JWT + Spring Security

Async Communication
├── Message Broker: Apache Kafka
└── Use Cases:      Notifications, Recording, Audit, Finance

Video Streaming
├── Server:   LiveKit SFU
├── Client:   LiveKit React SDK
└── Infra:    Load Balancer + Multiple LiveKit instances

Database
├── PostgreSQL: Core relational data
├── MongoDB:    Content & flexible schema
├── Redis:      Cache & sessions
└── S3/MinIO:   Video & file storage

AI/LLMs
├── Course Generation
├── Recommendation Engine
└── AI Tutor Chatbot

Infrastructure
├── Load Balancer: Nginx / AWS ALB
├── Container:     Docker + Kubernetes
└── Monitoring:    ELK Stack + Prometheus + Grafana
```

---

*Document created: May 11, 2026*
*Researcher: Bhushan*
*Based on: Unified LMS Architecture Diagram + Data Architect Discussion*
