# LiveKit — LMS Integration Notes
**Researcher:** Bhushan | **Date:** May 11, 2026

---

## Why LiveKit

| Reason | Detail |
|--------|--------|
| Open source + self-hosted | No vendor lock-in, full data control |
| Official Java SDK | Spring Boot ready — `io.livekit:livekit-server` |
| Official React SDK | `@livekit/components-react` — plug and play |
| Built-in recording | No separate recording service needed |
| SFU architecture | 1 instructor streams to 500+ students efficiently |
| Cost | Self-hosted = server cost only (vs Agora ~$15/1000 min) |

---

## Core Concepts

| Concept | Description |
|---------|-------------|
| **Room** | Virtual classroom with a unique name |
| **Participant** | Anyone in a room — instructor or student |
| **Track** | A single media stream — camera, mic, or screen share |
| **Token (JWT)** | Entry pass generated server-side; controls publish/subscribe permissions |
| **SFU** | Server receives one stream, forwards to all subscribers — scales to 500+ |
| **Egress** | LiveKit's recording system — saves room to MP4/S3 |
| **Webhook** | LiveKit calls your backend when room events happen |

---

## Backend Setup

### 1 — Maven Dependency

```xml
<dependency>
    <groupId>io.livekit</groupId>
    <artifactId>livekit-server</artifactId>
    <version>0.12.1</version>
</dependency>
```

> ⚠ **Common mistakes:**
> - `livekit-server-sdk` → does NOT exist on Maven Central
> - `livekit-server` → correct artifactId
> - version `0.10.1` → does not exist, use `0.12.1`
> - Always verify on `central.sonatype.com` before adding any dependency

---

### 2 — application.yml

```yaml
spring:
  application:
    name: lms-live-service
  datasource:
    url: jdbc:h2:mem:livedb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    database-platform: org.hibernate.dialect.H2Dialect

server:
  port: 8084

livekit:
  api:
    key: devkey
    secret: devsecret
  server:
    url: ws://localhost:7880        # frontend WebSocket connection
    api-url: http://localhost:7880  # backend RoomServiceClient HTTP REST
  webhook:
    secret: devsecret
```

| Property | Value | Used by |
|----------|-------|---------|
| `livekit.server.url` | `ws://localhost:7880` | React frontend — WebSocket video/audio |
| `livekit.server.api-url` | `http://localhost:7880` | Spring Boot `RoomServiceClient` — HTTP REST |
| `livekit.webhook.secret` | `devsecret` | Verify incoming webhook signatures |

> ⚠ **Two URLs are required.** Passing `ws://` to `RoomServiceClient` throws:
> `Expected URL scheme 'http' or 'https' but was 'ws'`

> `livekit.*` shows "Unknown property" warning in IDE — harmless. `@Value` still works fine.

---

### 3 — Docker (LiveKit Server)

**One-time setup:**
```bash
docker run -d -p 7880:7880 -p 7881:7881 -p 7882:7882/udp \
  -e LIVEKIT_KEYS="devkey: devsecret" \
  --name livekit-dev livekit/livekit-server --dev
```

**Daily commands:**
```bash
docker start livekit-dev    # start
docker stop livekit-dev     # stop
docker logs livekit-dev     # check logs
docker ps                   # verify running
```

| Flag | Meaning |
|------|---------|
| `-d` | Run in background (detached) — terminal stays free |
| `-p 7880:7880` | HTTP / WebSocket port |
| `-p 7881:7881` | RTC TCP media port |
| `-p 7882:7882/udp` | RTC UDP media port — `/udp` means UDP protocol |
| `-e LIVEKIT_KEYS` | API key:secret as environment variable |
| `--name livekit-dev` | Named container — reuse with `docker start` |
| `--dev` | Dev mode — relaxed security, verbose logs |
| `--rm` | ⚠ Avoid — auto-deletes container on stop |

> **Image vs Container:** `--rm` deletes the container, never the image.
> Image stays forever once pulled. Container is just a running instance.

---

### 4 — Package Structure

```
com.knowlia.lms_live_service
├── config/        → LiveKitConfig (bean), CorsConfig
├── model/         → LiveSession (entity)
├── repository/    → LiveSessionRepository (JPA)
├── dto/           → JoinRequest, JoinResponse, StartSessionRequest
├── service/       → LiveKitTokenService, LiveSessionService
├── controller/    → LiveSessionController
└── webhook/       → LiveKitWebhookController
```

---

### 5 — Code Build Order

| # | Class | Package | Purpose |
|---|-------|---------|---------|
| 1 | `LiveKitConfig` | `config` | `@Bean` for `RoomServiceClient` — reads `api-url`, `key`, `secret` from yml |
| 2 | `CorsConfig` | `config` | Allow React (`localhost:5173`) to call backend (`localhost:8084`) |
| 3 | `LiveSession` | `model` | JPA entity — one row per live class session |
| 4 | `LiveSessionRepository` | `repository` | JPA repo — `findByRoomName`, `existsByRoomNameAndStatus` |
| 5 | `JoinRequest / JoinResponse` | `dto` | Student join request and token response |
| 6 | `StartSessionRequest` | `dto` | Instructor start class request |
| 7 | `LiveKitTokenService` | `service` | Generate instructor + student JWT tokens |
| 8 | `LiveSessionService` | `service` | Business logic — create room, join checks, end session |
| 9 | `LiveSessionController` | `controller` | REST API — `/start`, `/join`, `/end`, `/status` |
| 10 | `LiveKitWebhookController` | `webhook` | Handle LiveKit room events |

---

### 6 — Token Generation (Correct API)

> `VideoGrant` is a **sealed class** — `new VideoGrant()` throws `Cannot instantiate`.
> Each permission is a separate subclass — this is the correct way:

```java
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import io.livekit.server.CanPublishData;
import io.livekit.server.RoomAdmin;

// Instructor — full control
token.addGrants(
    new RoomJoin(true),
    new RoomName(roomName),
    new CanPublish(true),       // camera / mic / screen share
    new CanSubscribe(true),
    new CanPublishData(true),   // chat via data channel
    new RoomAdmin(true)         // mute / kick participants
);
token.setTtl(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS));

// Student — view only
token.addGrants(
    new RoomJoin(true),
    new RoomName(roomName),
    new CanPublish(false),      // cannot broadcast
    new CanSubscribe(true),
    new CanPublishData(true)    // chat allowed
);
token.setTtl(TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS));
```

---

### 7 — REST API Endpoints

| Method | URL | Who calls | What it does |
|--------|-----|-----------|-------------|
| `POST` | `/api/live/start` | Instructor | Creates room in LiveKit, returns instructor token |
| `POST` | `/api/live/join` | Student | Checks room ACTIVE in DB, returns student token |
| `POST` | `/api/live/end?roomName=x` | Instructor | Deletes room from LiveKit, fires webhooks |
| `GET` | `/api/live/status?roomName=x` | Frontend | Returns 200 if ACTIVE, 404 if not |

---

### 8 — Webhook Events

| Event | When fired | Action taken |
|-------|-----------|-------------|
| `room_started` | Room created | Log / confirm session started |
| `room_finished` | Room deleted | Mark session ENDED in DB |
| `participant_joined` | Someone joined | Attendance tracking (Kafka — future) |
| `participant_left` | Someone left | Duration calculation (Kafka — future) |
| `egress_ended` | Recording file ready | Save S3 URL to DB, notify students |

> Always return `200 OK` quickly — LiveKit retries webhook on non-200 response.
> Verify signature using `WebhookReceiver` before processing — prevents fake calls.

---

## Frontend Setup

### 1 — Create React Project

```bash
npm create vite@latest LiveKit-Frontend -- --template react
cd LiveKit-Frontend
npm install
```

### 2 — Install LiveKit Packages

```bash
npm install @livekit/components-react livekit-client @livekit/components-styles
```

### 3 — Environment Variables (`.env`)

```
VITE_BACKEND_URL=http://localhost:8084
VITE_LIVEKIT_URL=ws://localhost:7880
```

> Vite requires `VITE_` prefix. Access via `import.meta.env.VITE_BACKEND_URL` in code.

### 4 — File Structure

```
src/
├── api/
│   └── liveApi.js           → all fetch calls to Spring Boot backend
├── components/
│   ├── RoleSelector.jsx     → choose instructor or student
│   ├── InstructorLobby.jsx  → form to start class → calls /api/live/start
│   ├── StudentLobby.jsx     → form to join class → calls /api/live/join
│   └── VideoConference.jsx  → camera, mic, screen share, leave button
└── App.jsx                  → screen navigation: role → lobby → room
```

### 5 — Key LiveKit Components

| Component | Purpose |
|-----------|---------|
| `LiveKitRoom` | Wraps entire room, manages WebSocket connection to LiveKit |
| `GridLayout` | Displays all participant video tiles in a grid |
| `ParticipantTile` | Single participant video or avatar placeholder |
| `ControlBar` | Mic, camera, screen share, leave buttons — built-in UI |
| `RoomAudioRenderer` | Plays all remote audio tracks — **must include or no sound** |
| `useTracks` | Hook — returns all active camera/screen tracks in room |
| `useRoomContext` | Hook — access room object to call `room.disconnect()` |

### 6 — Run

```bash
npm run dev    # starts at http://localhost:5173
npm run build  # production build → dist/
```

---

## Common Gotchas

### Identity Conflict
> Same `identity` joining same room twice → LiveKit **kicks the first connection**.

```js
// Auto-generate unique ID per session — prevents conflict in dev/testing
const [studentId] = useState(() => 'student-' + Math.random().toString(36).substring(2, 8));
```

In production, use real user ID from auth system — conflict never happens with real users.

---

### CORS Error (`Failed to fetch`)
> React on `localhost:5173` calling Spring Boot on `localhost:8084` = CORS blocked by browser.
> Fix: add `CorsConfig.java` in `config` package allowing `http://localhost:5173` on `/api/**`.
> After adding, **restart Spring Boot** — CORS config is not hot-reloaded.

---

### Which Endpoints Need Docker

| Endpoint | Needs Docker | Why |
|----------|-------------|-----|
| `/api/live/start` | ✅ Yes | Calls `createRoom()` on LiveKit server |
| `/api/live/end` | ✅ Yes | Calls `deleteRoom()` on LiveKit server |
| `/api/live/join` | ❌ No | Only checks DB + generates token |
| `/api/live/status` | ❌ No | Only checks DB |

---

## Recording Types

| Type | Output | Best for |
|------|--------|----------|
| **Composite** | Single MP4 — all participants in one file | LMS playback after class |
| **Track** | Separate file per participant | Post-production editing |
| **RTMP** | Live stream to YouTube / Facebook | Public broadcast |

---

## Full Service Flow

```
INSTRUCTOR
  → POST /api/live/start
  → Spring Boot: createRoom in LiveKit + save DB (ACTIVE) + generate instructor token
  → Instructor connects to LiveKit with token → camera/mic starts

STUDENT
  → POST /api/live/join
  → Spring Boot: check DB room is ACTIVE + generate student token
  → Student connects to LiveKit with token → sees instructor video/audio

CLASS ENDS
  → POST /api/live/end
  → Spring Boot: deleteRoom in LiveKit + mark DB (ENDED)
  → LiveKit fires webhooks → room_finished, egress_ended
  → Future: Kafka → Attendance Service, Recording Service, Notification Service
```

---

*Source: LiveKit Official Docs + Maven Central + hands-on POC*
