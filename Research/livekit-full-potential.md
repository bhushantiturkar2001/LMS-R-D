# LiveKit — Full Potential & Features Guide
**Researcher:** Bhushan
**Date:** May 18, 2026
**Source:** LiveKit Official Docs + Pricing Page

---

## Table of Contents

1. [What is LiveKit](#what-is-livekit)
2. [LiveKit Cloud Free Tier](#livekit-cloud-free-tier)
3. [Pricing Plans Comparison](#pricing-plans-comparison)
4. [All Features LiveKit Provides](#all-features-livekit-provides)
5. [Java SDK — All Available Methods](#java-sdk--all-available-methods)
6. [Quality Management per Student](#quality-management-per-student)
7. [Bandwidth Calculator for LMS](#bandwidth-calculator-for-lms)
8. [Self-Hosted vs Cloud](#self-hosted-vs-cloud)
9. [Webhook Events — Full List](#webhook-events--full-list)
10. [LMS Use Case Mapping](#lms-use-case-mapping)

---

## What is LiveKit

LiveKit is an open-source WebRTC SFU (Selective Forwarding Unit) platform for building real-time video, audio, and data applications.

```
Your App (React + Spring Boot)
        |
        v
  lms-live-service        <- Your wrapper microservice
        |
        v
  LiveKit SFU Server      <- Actual video engine
        |
        +---> Student 1
        +---> Student 2
        +---> Student 500
```

**Key Point:** LiveKit can be used in 2 ways:
- **LiveKit Cloud** — Managed service (they host it, you pay per usage)
- **Self-Hosted** — You run it on your own server (Docker), completely free

---

## LiveKit Cloud Free Tier

> Source: [livekit.io/pricing](https://livekit.io/pricing) — May 2026

### Build Plan — $0/month (FREE)

| Feature | Free Limit |
|---------|-----------|
| WebRTC Minutes | **5,000 minutes/month** |
| Concurrent Connections | **100 users** |
| Downstream Data Transfer | **50 GB/month** |
| Agent Session Minutes | 1,000 minutes |
| AI Inference Credits | $2.50 included |
| Telephony (US number) | 1 free number |
| Uptime SLA | 99.99% |
| No credit card required | YES |

### What 5,000 Free Minutes Means for LMS

```
5,000 minutes = ~83 hours of live class per month

Example:
- 1 class = 1 hour = 60 minutes
- 5,000 / 60 = ~83 classes per month FREE (single student)

But: Minutes are counted per participant!
- 1 class x 1 hour x 10 students = 600 minutes used
- 5,000 / 600 = ~8 classes with 10 students FREE
```

### What 50 GB Free Bandwidth Means

```
Video quality bandwidth usage:
- 360p  = ~0.5 Mbps = ~0.225 GB/hour per student
- 540p  = ~1.0 Mbps = ~0.450 GB/hour per student
- 720p  = ~2.5 Mbps = ~1.125 GB/hour per student
- 1080p = ~4.0 Mbps = ~1.800 GB/hour per student

50 GB free at 540p:
- 50 GB / 0.450 GB per student per hour
- = ~111 student-hours free per month
- = 10 students x 11 hours free
```

---

## Pricing Plans Comparison

| Feature | Build (FREE) | Ship ($50/mo) | Scale ($500/mo) |
|---------|-------------|--------------|----------------|
| WebRTC Minutes | 5,000 | 150,000 | 1.5M |
| Concurrent Users | 100 | 1,000 | 5,000 |
| Downstream Bandwidth | 50 GB | 250 GB | 3 TB |
| Extra Bandwidth | N/A | $0.12/GB | $0.10/GB |
| Extra WebRTC min | N/A | $0.0005/min | $0.0004/min |
| Team Collaboration | No | Yes | Yes |
| Role-based Access | No | No | Yes |
| HIPAA Compliance | No | No | Yes |
| Support | Community | Email | Priority |

---

## All Features LiveKit Provides

### Core Video/Audio Features

| Feature | Description | Free? |
|---------|-------------|-------|
| **Multi-party Video** | Multiple participants in one room | YES |
| **Audio Rooms** | Audio-only sessions (podcast style) | YES |
| **Screen Sharing** | Share full screen or specific window | YES |
| **HD Video** | Up to 1080p video quality | YES |
| **Adaptive Bitrate** | Auto quality adjust based on network | YES |
| **Simulcast** | Multiple quality streams simultaneously | YES |
| **Noise Cancellation** | Background noise reduction (Krisp) | YES |
| **Echo Cancellation** | Built-in WebRTC echo cancellation | YES |
| **End-to-End Encryption** | E2EE for sensitive sessions | YES |

### Room Management Features

| Feature | Description | Free? |
|---------|-------------|-------|
| **Create Room** | Create named rooms with config | YES |
| **Delete Room** | End room and disconnect all | YES |
| **List Rooms** | Get all active rooms | YES |
| **Room Metadata** | Attach custom data to room | YES |
| **Max Participants** | Set room capacity limit | YES |
| **Empty Timeout** | Auto-close empty rooms | YES |
| **Departure Timeout** | Grace period before room closes | YES |

### Participant Management Features

| Feature | Description | Free? |
|---------|-------------|-------|
| **List Participants** | Get all participants in room | YES |
| **Remove Participant** | Kick a participant from room | YES |
| **Mute Participant** | Mute someone's mic remotely | YES |
| **Update Metadata** | Set custom data per participant | YES |
| **Participant Permissions** | Control publish/subscribe per user | YES |
| **Send Data Message** | Send custom data to participants | YES |

### Recording Features (Egress)

| Feature | Description | Free? |
|---------|-------------|-------|
| **Room Composite Recording** | Record entire room as one MP4 | YES (self-hosted) |
| **Track Recording** | Record individual participant tracks | YES (self-hosted) |
| **Track Composite** | Record specific participant (video+audio) | YES (self-hosted) |
| **RTMP Streaming** | Stream to YouTube/Twitch/Facebook | YES (self-hosted) |
| **HLS Streaming** | HTTP Live Streaming output | YES (self-hosted) |
| **MP4 Output** | Record in MP4 format | YES |
| **S3 Upload** | Direct upload to AWS S3 | YES |
| **Local File** | Save to local filesystem | YES (self-hosted) |
| **Custom Layout** | Custom HTML layout for recording | YES |

### Stream Import Features (Ingress)

| Feature | Description | Free? |
|---------|-------------|-------|
| **RTMP Ingress** | Import RTMP stream into LiveKit room | YES |
| **WHIP Ingress** | WebRTC HTTP Ingest Protocol | YES |
| **URL Ingress** | Import from MP4/HLS URL | YES |
| **OBS Support** | Stream from OBS Studio | YES |

### Data & Messaging Features

| Feature | Description | Free? |
|---------|-------------|-------|
| **Data Channels** | Send arbitrary data between participants | YES |
| **Reliable Messages** | Guaranteed delivery data packets | YES |
| **Lossy Messages** | Low-latency, best-effort packets | YES |
| **Room-wide Broadcast** | Send message to all participants | YES |
| **Direct Message** | Send to specific participant | YES |

### Security Features

| Feature | Description | Free? |
|---------|-------------|-------|
| **JWT Tokens** | Signed access tokens | YES |
| **RBAC** | Role-based permissions per token | YES |
| **Token Expiry** | Set TTL on tokens | YES |
| **Webhook Verification** | HMAC signature on webhooks | YES |
| **TLS/DTLS** | Encrypted transport | YES |
| **SRTP** | Encrypted media streams | YES |

---

## Java SDK — All Available Methods

### RoomServiceClient — Room Management

```java
RoomServiceClient client = RoomServiceClient.create(url, apiKey, apiSecret);

// Room Operations
client.createRoom(CreateRoomRequest)
client.listRooms(List<String> roomNames)
client.deleteRoom(String roomName)
client.updateRoomMetadata(String roomName, String metadata)

// Participant Operations
client.listParticipants(String roomName)
client.getParticipant(String roomName, String identity)
client.removeParticipant(String roomName, String identity)
client.mutePublishedTrack(roomName, identity, trackSid, muted)
client.updateParticipant(roomName, identity, metadata, permission)
client.updateSubscriptions(roomName, identity, trackSids, subscribe)

// Data Messages
client.sendData(roomName, data, kind, destinationIdentities)
```

### EgressClient — Recording & Streaming

```java
EgressClient egressClient = EgressClient.create(url, apiKey, apiSecret);

// Room Composite (entire room as one video)
egressClient.startRoomCompositeEgress(roomName, output)
egressClient.startRoomCompositeEgress(roomName, output, options)

// Track Composite (specific participant)
egressClient.startTrackCompositeEgress(roomName, output, options)

// Track Egress (individual tracks)
egressClient.startTrackEgress(roomName, output, trackId)

// Web Egress (custom HTML layout)
egressClient.startWebEgress(url, output)

// Participant Egress
egressClient.startParticipantEgress(roomName, identity, output)

// Control
egressClient.stopEgress(egressId)
egressClient.updateLayout(egressId, layout)
egressClient.updateStream(egressId, addOutputUrls, removeOutputUrls)
egressClient.listEgress(roomName)
```

### IngressClient — Stream Import

```java
IngressClient ingressClient = IngressClient.create(url, apiKey, apiSecret);

ingressClient.createIngress(inputType, options)
ingressClient.updateIngress(ingressId, options)
ingressClient.listIngress(roomName)
ingressClient.deleteIngress(ingressId)
```

### AccessToken — Token Generation

```java
AccessToken token = new AccessToken(apiKey, apiSecret);

token.setIdentity(userId)           // Unique user ID — BIND to user
token.setName(displayName)          // Display name in room
token.setTtl(duration, timeUnit)    // Token expiry (5-10 min recommended)
token.addGrant(videoGrant)          // Add permissions

// VideoGrant permissions
VideoGrant grant = new VideoGrant();
grant.setRoomJoin(true)             // Can join room
grant.setRoom(roomName)             // Which room
grant.setCanPublish(true/false)     // Can send video/audio
grant.setCanSubscribe(true/false)   // Can receive video/audio
grant.setCanPublishData(true/false) // Can send data messages
grant.setRoomAdmin(true/false)      // Admin permissions (mute/kick)
grant.setRoomCreate(true/false)     // Can create rooms
grant.setRoomRecord(true/false)     // Can start recording
grant.setHidden(true/false)         // Hidden participant (for bots)

token.toJwt()                       // Generate JWT string
```

### WebhookReceiver — Webhook Verification

```java
WebhookReceiver receiver = new WebhookReceiver(apiKey, webhookSecret);
WebhookEvent event = receiver.receive(body, authHeader);
```

---

## Quality Management per Student

### The Problem

```
500 students enrolled — different internet speeds:
- Student A: 100 Mbps fiber   -> can handle 1080p
- Student B: 10 Mbps broadband-> can handle 720p
- Student C: 2 Mbps mobile    -> can handle 360p
- Student D: 0.5 Mbps slow    -> can handle 180p
```

### Solution 1: Simulcast (Recommended — Automatic)

Instructor publishes **3 quality streams simultaneously**:

```
Instructor Camera
    |
    +---> Low quality   (180p, 200kbps) -> Student D (slow internet)
    +---> Medium quality(360p, 500kbps) -> Student C (mobile)
    +---> High quality  (720p, 2Mbps)   -> Student A, B (good internet)
```

LiveKit **automatically selects** the right quality per student based on their bandwidth. No manual code needed — just enable simulcast.

### Solution 2: Quality Based on Enrolled Students

```
Students enrolled  | Recommended Quality | Bandwidth/student | Max on 1Gbps
1   - 100 students | 720p               | 2.5 Mbps          | 400 students
100 - 300 students | 540p               | 1.0 Mbps          | 500 students
300 - 500 students | 360p               | 0.5 Mbps          | 969 students
500+ students      | 240p               | 0.3 Mbps          | 1,500 students
```

Set quality in room metadata when creating session (Spring Boot):

```java
int enrolledStudents = enrollmentService.getCount(req.getCourseId());

String videoQuality;
if (enrolledStudents <= 100)       videoQuality = "720p";
else if (enrolledStudents <= 300)  videoQuality = "540p";
else if (enrolledStudents <= 500)  videoQuality = "360p";
else                               videoQuality = "240p";

// Store in room metadata — frontend reads this
CreateRoomRequest roomRequest = CreateRoomRequest.newBuilder()
    .setName(roomName)
    .setMaxParticipants(enrolledStudents + 10)
    .setMetadata("{\"quality\":\"" + videoQuality + "\"}")
    .build();
```

### Solution 3: Force Quality Server-Side

```java
// Force student to receive lower quality (save bandwidth)
roomServiceClient.updateSubscriptions(
    roomName,
    studentIdentity,
    List.of(instructorTrackSid),
    true,
    VideoQuality.LOW    // LOW=180p, MEDIUM=360p, HIGH=720p
);
```

---

## Bandwidth Calculator for LMS

### Per Student Bandwidth Usage

| Quality | Video Bitrate | Audio | Total/Student | 1 Hour Data |
|---------|-------------|-------|--------------|-------------|
| 180p | 200 kbps | 32 kbps | ~232 kbps | ~104 MB |
| 360p | 500 kbps | 32 kbps | ~532 kbps | ~239 MB |
| 540p | 1,000 kbps | 32 kbps | ~1,032 kbps | ~464 MB |
| 720p | 2,500 kbps | 32 kbps | ~2,532 kbps | ~1.1 GB |
| 1080p | 4,000 kbps | 32 kbps | ~4,032 kbps | ~1.8 GB |

### Max Students per 1 Gbps Server

| Quality | Per Student | Max Students |
|---------|------------|-------------|
| 180p | 232 kbps | ~4,310 |
| 360p | 532 kbps | ~1,879 |
| 540p | 1,032 kbps | ~969 ✅ (your arch says 500 — safe buffer) |
| 720p | 2,532 kbps | ~395 ✅ (your arch says 400 — matches) |
| 1080p | 4,032 kbps | ~248 |

### Monthly Bandwidth Estimate

```
Scenario: 100 students, 2 hours/day, 540p, 22 working days/month

Per student per class : 0.464 GB x 2 hours = 0.928 GB
100 students per class: 92.8 GB
22 days/month         : 92.8 x 22 = ~2,041 GB = ~2 TB/month

LiveKit Cloud cost    : 2,000 GB x $0.12 = $240/month (Ship plan)
Self-hosted cost      : ~$50-100/month VPS only
```

---

## Self-Hosted vs Cloud

| Factor | Self-Hosted (Docker) | LiveKit Cloud |
|--------|---------------------|--------------|
| Cost | Server cost only | Pay per usage |
| Setup | Docker command | Just API keys |
| Scaling | Manual | Automatic |
| Data Privacy | Full control | LiveKit servers |
| Recording storage | Local filesystem | S3/Cloud |
| Maintenance | You manage | They manage |
| Best for | Production LMS | POC/Testing |

### Self-Hosted Docker (Basic)

```bash
docker run --rm \
  -p 7880:7880 \
  -p 7881:7881 \
  -p 7882:7882/udp \
  -e LIVEKIT_KEYS="devkey: devsecret" \
  livekit/livekit-server \
  --dev
```

### Self-Hosted with Recording (Egress Service)

```bash
# Egress service needed separately for recording
docker run --rm \
  -e LIVEKIT_URL=ws://localhost:7880 \
  -e LIVEKIT_API_KEY=devkey \
  -e LIVEKIT_API_SECRET=devsecret \
  -v /local/recordings:/recordings \
  livekit/egress
```

---

## Webhook Events — Full List

| Event | When Fires | Use in LMS |
|-------|-----------|-----------|
| `room_started` | Room created | Mark session ACTIVE in DB |
| `room_finished` | Room deleted | Mark ENDED, trigger recording process |
| `participant_joined` | User connects | Mark attendance START time |
| `participant_left` | User disconnects | Mark attendance END, calculate duration |
| `track_published` | Camera/mic turned on | Log instructor started video |
| `track_unpublished` | Camera/mic turned off | Log instructor stopped video |
| `track_muted` | Track muted | Log mute event |
| `track_unmuted` | Track unmuted | Log unmute event |
| `egress_started` | Recording started | Mark recording STARTED in DB |
| `egress_updated` | Recording status change | Update recording progress |
| `egress_ended` | Recording finished | Save file URL, notify students |
| `ingress_started` | RTMP stream started | Log stream ingress |
| `ingress_ended` | RTMP stream ended | Log stream end |

---

## LMS Use Case Mapping

| LMS Feature | LiveKit Feature Used |
|-------------|---------------------|
| Live Class | Room + Participants |
| Instructor teaches | Publisher token (`canPublish: true`) |
| Student watches | Subscriber token (`canPublish: false`) |
| Screen share | ScreenShare track source |
| Class recording | Egress — Room Composite → MP4 |
| Chat during class | Data Channel (reliable) |
| Raise hand | Data Channel + Participant Metadata |
| Mute student | `mutePublishedTrack()` |
| Kick student | `removeParticipant()` |
| Class capacity limit | `maxParticipants` in CreateRoomRequest |
| Quality per class | Room metadata + Simulcast |
| Attendance tracking | `participant_joined` / `participant_left` webhooks |
| Recording available | `egress_ended` webhook → notify students |
| Live streaming to YouTube | RTMP Egress |
| Import pre-recorded video | URL Ingress |
| AI Tutor bot | Hidden participant + Agent |
| Token security | `identity` bound to userId + short TTL |
| Prevent token sharing | One active connection per identity |

---

---

## Live Recording — How It Works During Class

### Key Point — Recording Starts WHILE Class is Live

```
Instructor starts class
        |
        v
LiveKit Room created
        |
        v
Recording starts IMMEDIATELY (Egress service)
        |
        v  (class is running, students watching)
        |
        v
Class ends → Recording file saved to local folder
        |
        v
Students can watch recording later
```

Recording aur live class **simultaneously** chalte hain. Students live dekh rahe hain, saath mein file bhi ban rahi hai.

---

### 3 Ways to Record in LMS

#### Option 1: Auto-Record (Recommended for LMS)
Class start hote hi recording automatically shuru ho jaye.

```
Instructor starts class
    → Spring Boot creates room
    → Spring Boot immediately starts Egress (recording)
    → Class + Recording both running
    → Class ends → recording file ready
```

#### Option 2: Manual Record (Instructor Controls)
Instructor khud decide kare kab record karna hai.

```
Instructor clicks "Start Recording" button
    → Frontend calls POST /api/live/record/start
    → Spring Boot starts Egress
    → Instructor clicks "Stop Recording"
    → File saved
```

#### Option 3: Webhook Triggered
LiveKit `room_started` webhook aane pe recording shuru karo.

```
LiveKit fires room_started webhook
    → Spring Boot webhook handler receives it
    → Automatically starts Egress
    → No manual trigger needed
```

---

### Recording Output Options (All Free on Private Server)

| Output Type | Format | Where Saved | Use Case |
|-------------|--------|-------------|---------|
| Local File | MP4 | Your server folder | LMS recordings |
| Local File | WebM | Your server folder | Alternative format |
| S3 Bucket | MP4 | AWS/MinIO | Production storage |
| RTMP Stream | Live | YouTube/Twitch | Live streaming |
| HLS | .m3u8 | Your server | Adaptive streaming |

**For local development:** MP4 → local folder (simplest)
**For production:** MP4 → S3 or MinIO

---

### Recording Layout Options

#### Layout 1: Speaker View
```
+---------------------------+
|                           |
|    Instructor (big)       |
|                           |
+---------------------------+
| S1 | S2 | S3 | S4 | S5   |  <- students small
+---------------------------+
```

#### Layout 2: Grid View
```
+----------+----------+
| Instructor| Student1|
+----------+----------+
| Student2 | Student3 |
+----------+----------+
```

#### Layout 3: Instructor Only (Best for LMS)
```
+---------------------------+
|                           |
|    Instructor Only        |
|    (students not shown)   |
|                           |
+---------------------------+
```
Most LMS platforms record only instructor — saves storage, students don't need to be in recording.

---

### Local Storage — Where Files Go

```
Your Machine / Server
└── /recordings/
    └── physics-101/
        ├── 2026-05-18_10-00-00.mp4    <- Class 1 recording
        ├── 2026-05-18_14-00-00.mp4    <- Class 2 recording
        └── 2026-05-19_10-00-00.mp4    <- Class 3 recording
```

File size estimate:
```
1 hour class at 720p  = ~1.8 GB
1 hour class at 540p  = ~0.9 GB
1 hour class at 360p  = ~0.4 GB
```

---

### Egress Docker — Required for Recording

LiveKit Server alone recording nahi kar sakta. Egress service alag Docker container hai:

```bash
# Terminal 1: LiveKit Server
docker run --rm \
  -p 7880:7880 -p 7881:7881 -p 7882:7882/udp \
  -e LIVEKIT_KEYS="devkey: devsecret" \
  livekit/livekit-server --dev

# Terminal 2: Egress Service (for recording)
docker run --rm \
  -e LIVEKIT_URL=ws://localhost:7880 \
  -e LIVEKIT_API_KEY=devkey \
  -e LIVEKIT_API_SECRET=devsecret \
  -v D:/LMS/recordings:/recordings \
  livekit/egress
```

Both free. Both open source. Dono locally run hote hain.

---

### Recording Lifecycle — Full Flow

```
1. Class starts
        |
2. Egress starts → file begins writing to /recordings/
        |
3. Class running (live + recording simultaneously)
        |
4. Class ends → Egress stops → file finalized
        |
5. LiveKit fires webhook: egress_ended
        |
6. Spring Boot receives webhook → saves file path to DB
        |
7. Student opens "Recordings" page → sees the MP4
        |
8. Student watches recording
```

---

*Document created: May 18, 2026*
*Researcher: Bhushan*
*Sources: LiveKit Official Docs, LiveKit Pricing Page (livekit.io/pricing)*
