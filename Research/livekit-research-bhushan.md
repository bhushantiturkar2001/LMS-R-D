# LiveKit — LMS Integration Notes
**Researcher:** Bhushan | **Date:** May 11, 2026

---

## Why LiveKit

- Open source, self-hosted → no vendor lock-in, full data control
- Official **Java SDK** (Spring Boot ready) + Official **React SDK**
- Built-in recording (no separate recording service needed)
- SFU architecture → 1 instructor streams to 500+ students efficiently
- Self-hosted cost = server only (vs Agora ~$15/1000 min, Zoom ~$25/1000 min)

---

## Core Concepts

| Concept | Description |
|---------|-------------|
| **Room** | Virtual classroom with a unique name |
| **Participant** | Anyone in a room (instructor or student) |
| **Track** | A single media stream — camera, mic, or screen share |
| **Token (JWT)** | Entry pass generated server-side; controls publish/subscribe permissions |
| **SFU** | Server receives one stream, forwards to all subscribers — scales to 500+ |

---

## Backend Setup — Step by Step

### Step 1 — Maven Dependency

```xml
<!-- Correct artifact — livekit-server-sdk does NOT exist on Maven Central -->
<dependency>
    <groupId>io.livekit</groupId>
    <artifactId>livekit-server</artifactId>
    <version>0.12.1</version>
</dependency>
```

> **Common mistakes:**
> - artifactId `livekit-server-sdk` does not exist → correct is `livekit-server`
> - version `0.10.1` does not exist → use `0.12.1`
> - Always verify artifact name and version on `central.sonatype.com` before adding any dependency

---

### Step 2 — application.yml

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
    url: ws://localhost:7880        # frontend uses this — WebSocket connection
    api-url: http://localhost:7880  # backend RoomServiceClient uses this — HTTP REST
  webhook:
    secret: devsecret
```

> - `livekit.*` shows **"Unknown property"** warning in IDE — harmless, `@Value` still works fine
> - `ws://` = WebSocket — persistent two-way connection. Local: `ws://`, Production: `wss://`
> - `webhook.secret` = LiveKit signs webhook payloads with this. Same as `api.secret` in dev
> - **Two URLs needed** — passing `ws://` to `RoomServiceClient` throws: `Expected URL scheme 'http' or 'https' but was 'ws'`

| Property | Value | Used by |
|----------|-------|---------|
| `livekit.server.url` | `ws://localhost:7880` | React frontend — WebSocket video/audio |
| `livekit.server.api-url` | `http://localhost:7880` | Spring Boot `RoomServiceClient` — HTTP REST |

---

### Step 3 — Docker (LiveKit Server)

**First time only:**
```bash
docker run -d -p 7880:7880 -p 7881:7881 -p 7882:7882/udp -e LIVEKIT_KEYS="devkey: devsecret" --name livekit-dev livekit/livekit-server --dev
```

**Every day after that:**
```bash
docker start livekit-dev   # start
docker stop livekit-dev    # stop
docker logs livekit-dev    # check logs
```

| Flag | Meaning |
|------|---------|
| `-d` | Run in background (detached) |
| `-p 7880:7880` | HTTP / WebSocket port |
| `-p 7881:7881` | RTC TCP media port |
| `-p 7882:7882/udp` | RTC UDP media port |
| `-e LIVEKIT_KEYS` | API key:secret passed as env variable |
| `--name livekit-dev` | Container name — reuse with `docker start` |
| `--dev` | Dev mode — relaxed security, verbose logs |
| `--rm` | ⚠ Avoid — auto-deletes container on stop |

> Image vs Container: `--rm` deletes the container, never the image. Image stays forever once pulled.

---

### Step 4 — Package Structure

```
com.knowlia.lms_live_service
├── config/        → LiveKit bean config, CORS config
├── model/         → Entity (LiveSession)
├── repository/    → JPA repo
├── dto/           → Request / Response objects
├── service/       → Token generation, session logic
├── controller/    → REST endpoints
└── webhook/       → LiveKit event handler
```

---

### Step 5 — Code Build Order

| Order | Class | Package | Purpose |
|-------|-------|---------|---------|
| 1 | `LiveKitConfig` | `config` | `@Bean` for `RoomServiceClient` — inject `api-url`, `key`, `secret` |
| 2 | `CorsConfig` | `config` | Allow React frontend (`localhost:5173`) to call backend |
| 3 | `LiveSession` | `model` | DB entity for session tracking |
| 4 | `LiveSessionRepository` | `repository` | JPA repo |
| 5 | `JoinRequest / JoinResponse` | `dto` | Student join request/response |
| 6 | `StartSessionRequest` | `dto` | Instructor start class request |
| 7 | `LiveKitTokenService` | `service` | Token generation — instructor + student |
| 8 | `LiveSessionService` | `service` | Business logic — create room, join, end session |
| 9 | `LiveSessionController` | `controller` | REST API — `/start`, `/join`, `/end`, `/status` |
| 10 | `LiveKitWebhookController` | `webhook` | Handle LiveKit events |

---

### Step 6 — Token Generation (Correct API)

> `VideoGrant` is a **sealed class** — cannot do `new VideoGrant()`.
> Each permission is a separate subclass. This is the correct way:

```java
// Correct imports
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import io.livekit.server.CanPublishData;
import io.livekit.server.RoomAdmin;

// Instructor token
token.addGrants(
    new RoomJoin(true),
    new RoomName(roomName),
    new CanPublish(true),       // can share camera/mic/screen
    new CanSubscribe(true),
    new CanPublishData(true),   // chat
    new RoomAdmin(true)         // mute/kick participants
);
token.setTtl(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS));

// Student token
token.addGrants(
    new RoomJoin(true),
    new RoomName(roomName),
    new CanPublish(false),      // view only
    new CanSubscribe(true),
    new CanPublishData(true)    // chat allowed
);
token.setTtl(TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS));
```

---

### Step 7 — REST API Endpoints

| Method | URL | Who | What |
|--------|-----|-----|------|
| `POST` | `/api/live/start` | Instructor | Creates room, returns instructor token |
| `POST` | `/api/live/join` | Student | Validates room active, returns student token |
| `POST` | `/api/live/end?roomName=x` | Instructor | Deletes room, fires webhooks |
| `GET` | `/api/live/status?roomName=x` | Frontend | Check if class is live |

---

### Step 8 — Webhook Events

| Event | When | Action |
|-------|------|--------|
| `room_started` | Room created | Log / DB update |
| `room_finished` | Room deleted | Mark session ENDED |
| `participant_joined` | Someone joined | Attendance (Kafka later) |
| `participant_left` | Someone left | Duration calc (Kafka later) |
| `egress_ended` | Recording ready | Save S3 URL to DB |

> Always return `200 OK` quickly from webhook — LiveKit retries on non-200.
> Verify signature first using `WebhookReceiver` — prevents fake webhook calls.

---

## Frontend Setup — Step by Step

### Step 1 — Create React Project

```bash
npm create vite@latest LiveKit-Frontend -- --template react
cd LiveKit-Frontend
npm install
```

### Step 2 — Install LiveKit Packages

```bash
npm install @livekit/components-react livekit-client @livekit/components-styles
```

### Step 3 — Environment Variables

Create `.env` in project root:
```
VITE_BACKEND_URL=http://localhost:8084
VITE_LIVEKIT_URL=ws://localhost:7880
```

> Vite uses `VITE_` prefix for env variables. Access in code via `import.meta.env.VITE_BACKEND_URL`.

### Step 4 — File Structure

```
src/
├── api/
│   └── liveApi.js          → fetch calls to Spring Boot backend
├── components/
│   ├── RoleSelector.jsx    → choose instructor or student
│   ├── InstructorLobby.jsx → enter details, call /api/live/start
│   ├── StudentLobby.jsx    → enter room name, call /api/live/join
│   └── VideoConference.jsx → camera, mic, screen share, leave button
└── App.jsx                 → screen navigation (role → lobby → room)
```

### Step 5 — Key Components Used

| Component | From | Purpose |
|-----------|------|---------|
| `LiveKitRoom` | `@livekit/components-react` | Wraps entire room, manages connection |
| `GridLayout` | `@livekit/components-react` | Shows all participant video tiles |
| `ParticipantTile` | `@livekit/components-react` | Single participant video/avatar |
| `ControlBar` | `@livekit/components-react` | Mic, camera, screen share, leave buttons |
| `RoomAudioRenderer` | `@livekit/components-react` | Plays all remote audio — must include |
| `useTracks` | `@livekit/components-react` | Hook to get all active tracks in room |
| `useRoomContext` | `@livekit/components-react` | Access room object to call `disconnect()` |

### Step 6 — Run Frontend

```bash
npm run dev
```

Opens at `http://localhost:5173`

---

## Important Gotchas

### Identity Conflict
> Same `identity` joining same room twice → LiveKit kicks the first connection.
> In dev, use unique IDs per tab. In production, use real user IDs from auth system.

```js
// Auto-generate unique ID to avoid conflict in dev/testing
const [studentId] = useState(() => 'student-' + Math.random().toString(36).substring(2, 8));
```

### CORS
> React (`localhost:5173`) calling Spring Boot (`localhost:8084`) requires CORS config.
> Add `CorsConfig.java` in `config` package — allow `http://localhost:5173` on `/api/**`.

### Endpoints that need Docker running

| Endpoint | Needs Docker |
|----------|-------------|
| `/api/live/start` | ✅ calls `createRoom` on LiveKit |
| `/api/live/end` | ✅ calls `deleteRoom` on LiveKit |
| `/api/live/join` | ❌ only checks DB + generates token |
| `/api/live/status` | ❌ only checks DB |

---

## Recording

| Type | Output | Use Case |
|------|--------|----------|
| **Composite** | Single MP4 (all participants) | LMS playback — recommended |
| **Track** | Separate file per participant | Post-editing |
| **RTMP** | Stream to YouTube/Facebook | Public live stream |

---

## Service Flow (LMS)

```
Instructor → POST /api/live/start → Spring Boot creates room in LiveKit → returns token
Instructor connects to LiveKit with token → starts camera/mic

Student → POST /api/live/join → Spring Boot checks DB → returns student token
Student connects to LiveKit with token → sees instructor video/audio

Class ends → instructor calls /api/live/end → LiveKit deletes room
LiveKit fires webhooks → Spring Boot updates DB
Kafka events (future) → Attendance, Recording, Notification services
```

---

*Source: LiveKit Official Docs + Maven Central verification + hands-on POC*
