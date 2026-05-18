# Javadoc, Comments & How to Read Documentation
**Bhushan | May 2026**

---

## The Golden Rule

> Comment the **WHY**, not the **WHAT**.
> Code shows what. Comments explain intent, business rules, and non-obvious decisions.

---

## 3 Types of Comments in Java

| Type | Syntax | When to use |
|------|--------|-------------|
| **Javadoc** | `/** */` | Classes, public methods, fields — generates HTML docs |
| **Block** | `/* */` | Multi-line explanation inside method — rarely needed |
| **Inline** | `//` | Single non-obvious line inside method body |

---

## Class-Level Javadoc

```java
/**
 * Generates signed JWT tokens for LiveKit room access.
 *
 * <p>Tokens are created server-side to prevent clients from forging
 * their own permissions. A student token has canPublish=false by default.
 * To allow a student to unmute for Q&A, update permissions via
 * {@link RoomServiceClient#updateParticipant} — do NOT issue a new token.</p>
 *
 * @author Bhushan
 * @since 1.0
 * @see RoomServiceClient
 */
@Service
public class LiveKitTokenService {
```

**What to include:**
- One sentence — what this class is responsible for
- Business rules or constraints future devs must know
- What NOT to do — saves next dev from wrong approach
- `@author`, `@since` in team projects
- `@see` to link related classes

---

## Method-Level Javadoc

```java
/**
 * Generates a student token for joining a LiveKit room.
 *
 * <p>Student tokens are subscribe-only by default.
 * canPublish=false — students cannot broadcast video or audio.
 * canPublishData=true — allows chat via data channel.</p>
 *
 * @param roomName    LiveKit room identifier, e.g. "physics-101-abc123"
 * @param studentId   unique student ID from DB — used as LiveKit identity
 * @param studentName display name shown to other participants
 * @return signed JWT string to pass to LiveKit client SDK
 * @throws IllegalStateException if identity is null or empty
 */
public String generateStudentToken(String roomName, String studentId, String studentName) {
```

**What to include:**
- First line — one sentence summary
- Business rules not obvious from code
- `@param` for every parameter — meaning, not just type
- `@return` — what the value represents
- `@throws` — when and why

---

## Inline Comments

```java
// Short UUID suffix avoids room name collisions across concurrent sessions
String roomName = req.getCourseId() + "-" + UUID.randomUUID().toString().substring(0, 8);

// emptyTimeout=300 — LiveKit auto-deletes room if empty for 5 min
// prevents ghost rooms if instructor disconnects without ending class
roomServiceClient.createRoom(roomName, 300, 500).execute();

// Status must be ACTIVE before students can join — checked in joinSession()
session.setStatus(SessionStatus.ACTIVE);
```

**Rules:**
- Only write when the line is non-obvious
- Explain the business reason, not the syntax
- One line max — if you need more, use method Javadoc

---

## What NOT to Comment

```java
// BAD — states the obvious, adds zero value
int count = 0;          // initialize count to zero
list.add(item);         // add item to list
return token;           // return the token

// BAD — repeats the method name
// This method generates a token
public String generateToken() {

// GOOD — explains a non-obvious business decision
// TTL 4h for instructor — longer because they prep before class starts
token.setTtl(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS));
```

---

## Javadoc Tags Reference

| Tag | Purpose | Example |
|-----|---------|---------|
| `@param` | Document a parameter | `@param roomName the LiveKit room ID` |
| `@return` | Document return value | `@return signed JWT string` |
| `@throws` | Document exception condition | `@throws RuntimeException if room not found` |
| `@author` | Who wrote it | `@author Bhushan` |
| `@since` | Version added | `@since 1.0` |
| `@see` | Link to related class/method | `@see RoomServiceClient` |
| `@deprecated` | Mark as outdated | `@deprecated use generateToken() instead` |
| `{@link}` | Inline link to class/method | `{@link LiveSessionService#startSession}` |
| `{@code}` | Inline code formatting | `{@code canPublish=false}` |

---

## Industry Patterns by Layer

### Controller
```java
/**
 * REST endpoint for students to join an active live class.
 * Returns 403 if not enrolled, 404 if class not started.
 *
 * @param req roomName, studentId, studentName, courseId
 * @return JoinResponse with LiveKit token and server URL
 */
@PostMapping("/join")
public ResponseEntity<JoinResponse> joinClass(@RequestBody JoinRequest req) {
```

### Service
```java
/**
 * Validates room is ACTIVE then issues student token.
 *
 * @param roomName  must match an ACTIVE session in DB
 * @param studentId LiveKit participant identity — must be unique per room
 * @return JoinResponse with token and server URL
 * @throws RuntimeException if no ACTIVE session found for roomName
 */
public JoinResponse joinSession(String roomName, String studentId, String studentName) {
```

### Entity / Model
```java
/**
 * Represents a single live class session.
 * One row created when instructor starts, updated when class ends.
 */
@Entity
public class LiveSession {

    /** S3 URL set after recording completes. Null during active session. */
    private String recordingUrl;

    /** Updated to ENDED by webhook handler after room_finished event. */
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
}
```

### Config
```java
/**
 * Creates LiveKit client beans.
 * Values injected from application.yml — never hardcode API keys.
 */
@Configuration
public class LiveKitConfig {
```

---

## Generate Javadoc

**Command:**
```bash
mvn javadoc:javadoc
```

**View output:**
```
d:\LMS\Livekit\lms-live-service\target\reports\apidocs\index.html
```

Or in browser:
```
file:///D:/LMS/Livekit/lms-live-service/target/reports/apidocs/index.html
```

**In STS — hover tooltip:**
Hover over any method name → Javadoc popup appears instantly. No generation needed.

**pom.xml config to stop warnings breaking build:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <configuration>
        <failOnWarnings>false</failOnWarnings>
        <doclint>none</doclint>
    </configuration>
</plugin>
```

| Config | Why |
|--------|-----|
| `failOnWarnings=false` | Missing `@param`/`@return` don't break the build |
| `doclint=none` | Disables strict HTML validation in comments |

> ⚠ **Common error — `semicolon missing`:**
> Caused by `(e.g. Q&A)` — Javadoc parser treats `&` as HTML entity start.
> Fix: write `for Q&A sessions` instead of `(e.g. Q&A)`.

---

## Comment Quality Checklist

Before committing, verify:

- [ ] Every public class has a Javadoc
- [ ] Every public method has a Javadoc
- [ ] All `@param` tags have meaningful descriptions (not just type)
- [ ] `@throws` documents the condition, not just the exception type
- [ ] Inline comments explain WHY, not WHAT
- [ ] No obvious comments that state what the code already shows
- [ ] Business rules are documented so a new dev understands without asking

---

## How to Read Documentation & Debug SDK Errors

### Debugging Order

```
Error appears
    ↓
1. Read the error message carefully — it's usually precise
    ↓
2. Check source jar (Ctrl+click in STS) — never lies
    ↓
3. Official docs — for concepts and config
    ↓
4. Maven Central — for correct artifact name and version
    ↓
5. Google / StackOverflow — last resort only
```

---

### Step 1 — Read the Error

`Cannot instantiate the type VideoGrant` tells you exactly:
- `VideoGrant` cannot be `new`-ed
- It's an interface, abstract class, or sealed class
- The error answered the question before any Googling

Train yourself to extract meaning from errors — they are precise.

---

### Step 2 — Read the Source Jar

When docs are wrong or outdated, **source code never lies**.

```bash
# Extract sources jar
jar xf livekit-server-0.12.1-sources.jar

# Read the class
Get-Content io/livekit/server/VideoGrant.kt
```

Result:
```kotlin
sealed class VideoGrant(val key: String, val value: Any)
class RoomJoin(value: Boolean) : VideoGrant("roomJoin", value)
class CanPublish(value: Boolean) : VideoGrant("canPublish", value)
```

Sealed class = cannot instantiate. Each permission is its own subclass. Ground truth.

**In STS — easier:**
- `Ctrl + click` any class name → jumps to source
- Right click jar → `Properties` → attach sources jar → every `Ctrl+click` shows real code

---

### Step 3 — Official Docs

Don't read top to bottom. Use like a dictionary:

```
1. Know what you want  →  "create a room in LiveKit"
2. Search that phrase in docs
3. Find method signature
4. Read @param and @return only
5. Copy minimal example → run → fix from error message
```

| SDK | Best source |
|-----|------------|
| LiveKit Java | GitHub README — `github.com/livekit/server-sdk-java` |
| Spring Boot | `docs.spring.io` — excellent, always current |
| Any SDK | GitHub repo → README → Java/usage section |

---

### Step 4 — Maven Central

Never trust a blog for artifact names or versions:

```
central.sonatype.com → search → verify artifact ID and version exist
```

How we caught the LiveKit mistake:
- `livekit-server-sdk` → does not exist
- `livekit-server` → correct
- `0.10.1` → does not exist
- `0.12.1` → correct

---

### Step 5 — Google / StackOverflow

| Good for | Not good for |
|----------|-------------|
| Common Spring Boot config | Specific SDK version APIs — outdated |
| Generic Java errors | Artifact names — blogs copy mistakes |
| "How to do X in framework Y" | Anything version-specific |

---

## Mental Model — 4 Things You Need from Any Doc

```
You want to: Generate a LiveKit token
    ↓
1. What class?      → AccessToken
2. What method?     → addGrants(), toJwt()
3. What params?     → Ctrl+click or read @param
4. What returns?    → signed JWT string

Write code → run → fix from error message
```

Everything else is noise until you need it.

---

## Quick Reference

| Problem | Where to look |
|---------|--------------|
| Wrong artifact name | `central.sonatype.com` |
| Method doesn't exist | Source jar — `Ctrl+click` in STS |
| Spring Boot config | `docs.spring.io` |
| SDK usage example | GitHub README |
| Generic Java error | StackOverflow |
| Cannot instantiate | Read the class — interface / abstract / sealed |
| Version compatibility | Maven Central release notes or GitHub releases |

---

## Naming Conventions

### Pattern: `[Domain] + [Layer]`

```
LiveSession  + Controller  = LiveSessionController
LiveKit      + Config      = LiveKitConfig
LiveSession  + Service     = LiveSessionService
LiveSession  + Repository  = LiveSessionRepository
LiveKit      + Webhook     + Controller = LiveKitWebhookController
```

New developer reads file name → knows exactly what's inside without opening it.

### Why not just `WebhookController`?

```
LiveKitWebhookController    ← LiveKit room events
PaymentWebhookController    ← Razorpay/Stripe events
SmsWebhookController        ← SMS delivery status
```

`WebhookController` alone tells you nothing. Specific names are precise.

### Package Naming

| Package | Contains |
|---------|---------|
| `controller/` | HTTP entry points called by frontend |
| `webhook/` | Callbacks from external systems — LiveKit, Razorpay |
| `service/` | Business logic |
| `repository/` | DB access |
| `config/` | Spring beans and configuration |
| `dto/` | Request / response objects |
| `model/` | JPA entities |

Webhook gets its own package — it's called by an external system, not your frontend. Keeping it separate makes that architectural difference visible in the folder structure.

---

*Good comments are a gift to your future self and your teammates.*
*Read errors carefully. Check source first. Docs second. Google last.*
