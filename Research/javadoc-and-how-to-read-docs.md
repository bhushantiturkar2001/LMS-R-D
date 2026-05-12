# Javadoc, Code Comments & How to Read Documentation
**Bhushan | May 2026**

---

## The Golden Rule

> Comment the **WHY**, not the **WHAT**.
> Code already shows what. Comments explain intent, business rules, and non-obvious decisions.

---

## 3 Types of Comments in Java

### 1. Javadoc — `/** */`
For classes, methods, and fields. Generates HTML documentation.

### 2. Block Comment — `/* */`
For multi-line explanations inside a method. Rarely used.

### 3. Inline Comment — `//`
For single non-obvious lines inside a method body.

---

## Class-Level Javadoc

```java
/**
 * Generates signed JWT tokens for LiveKit room access.
 *
 * <p>Tokens are created server-side to prevent clients from forging
 * their own permissions. A student token has canPublish=false by default.
 * To allow a student to unmute (e.g. Q&A), update permissions via
 * {@link RoomServiceClient#updateParticipant} — do NOT issue a new token.</p>
 *
 * @author Bhushan
 * @since 1.0
 * @see RoomServiceClient
 */
@Service
public class LiveKitTokenService {
```

**What to include at class level:**
- What this class is responsible for (one sentence)
- Important business rules or constraints
- What NOT to do (saves future devs from mistakes)
- `@author` and `@since` in team projects
- `@see` to link related classes

---

## Method-Level Javadoc

```java
/**
 * Generates a student token for joining a LiveKit room.
 *
 * <p>Student tokens are restricted to subscribe-only by default.
 * canPublish is set to false — students cannot broadcast video or audio.
 * canPublishData is true to allow chat messages via data channel.</p>
 *
 * @param roomName    the LiveKit room identifier (e.g. "physics-101-abc123")
 * @param studentId   unique student ID from the users DB — used as identity
 * @param studentName display name shown to other participants in the room
 * @return signed JWT string to be passed to LiveKit client SDK
 * @throws IllegalStateException if identity is null or empty
 */
public String generateStudentToken(String roomName, String studentId, String studentName) {
```

**What to include at method level:**
- What it does (first line, one sentence)
- Business rules that aren't obvious from the code
- `@param` for every parameter — what it means, not just its type
- `@return` — what the return value represents
- `@throws` — when and why it throws

---

## Inline Comments

```java
public JoinResponse startSession(StartSessionRequest req) throws Exception {

    // Short UUID suffix avoids room name collisions across concurrent sessions
    String roomName = req.getCourseId() + "-" + UUID.randomUUID().toString().substring(0, 8);

    // emptyTimeout=300 — LiveKit auto-deletes room if empty for 5 min
    // prevents ghost rooms if instructor disconnects without ending class
    roomServiceClient.createRoom(roomName, 300, 500).execute();

    // Status must be ACTIVE before students can join — checked in joinSession()
    session.setStatus(SessionStatus.ACTIVE);
}
```

**Rules for inline comments:**
- Only write when the line is non-obvious
- Explain the business reason, not the syntax
- Keep it short — one line max

---

## What NOT to Comment

```java
// BAD — states the obvious
int count = 0; // initialize count to zero
list.add(item); // add item to list
return token; // return the token

// BAD — repeats the method name
// This method generates a token
public String generateToken() {

// GOOD — explains a non-obvious decision
// TTL is 4 hours for instructor — longer than student (2h)
// because instructors often prep before class starts
token.setTtl(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS));
```

---

## Javadoc Tags Reference

| Tag | Usage | Example |
|-----|-------|---------|
| `@param` | Document a parameter | `@param roomName the LiveKit room ID` |
| `@return` | Document return value | `@return signed JWT string` |
| `@throws` | Document exception | `@throws RuntimeException if room not found` |
| `@author` | Who wrote it | `@author Bhushan` |
| `@since` | Version it was added | `@since 1.0` |
| `@see` | Link to related class/method | `@see RoomServiceClient` |
| `@deprecated` | Mark as outdated | `@deprecated use generateToken() instead` |
| `{@link}` | Inline link to class/method | `{@link LiveSessionService#startSession}` |
| `{@code}` | Inline code formatting | `{@code canPublish=false}` |

---

## Industry Patterns — What Big Teams Do

### Controller Layer
```java
/**
 * REST endpoint for students to join an active live class.
 *
 * <p>Validates enrollment before generating token.
 * Returns 403 if student is not enrolled, 404 if class has not started.</p>
 *
 * @param req contains roomName, studentId, studentName, courseId
 * @return JoinResponse with LiveKit token and server URL
 */
@PostMapping("/join")
public ResponseEntity<JoinResponse> joinClass(@RequestBody JoinRequest req) {
```

### Service Layer
```java
/**
 * Core business logic for joining a session.
 * Checks room is ACTIVE before issuing token.
 *
 * @param roomName  must match an ACTIVE session in DB
 * @param studentId used as LiveKit participant identity — must be unique per room
 * @return JoinResponse with token and server URL
 * @throws RuntimeException if no ACTIVE session found for roomName
 */
public JoinResponse joinSession(String roomName, String studentId, String studentName) {
```

### Entity / Model Layer
```java
/**
 * Represents a single live class session.
 * One row is created when instructor starts class,
 * updated when class ends or recording becomes available.
 */
@Entity
@Table(name = "live_sessions")
public class LiveSession {

    /** S3 URL populated after recording processing completes. Null during active session. */
    private String recordingUrl;

    /** Set to ENDED by webhook handler after room_finished event from LiveKit. */
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
}
```

### Config Layer
```java
/**
 * Spring configuration for LiveKit server clients.
 * Creates singleton beans used across all services.
 *
 * <p>Values are injected from application.yml.
 * Never hardcode API keys — use environment variables in production.</p>
 */
@Configuration
public class LiveKitConfig {
```

---

## Comment Quality Checklist

Before committing code, ask yourself:

- [ ] Does every public class have a Javadoc?
- [ ] Does every public method have a Javadoc?
- [ ] Are all `@param` tags filled with meaningful descriptions?
- [ ] Are `@throws` documented with the condition that causes them?
- [ ] Are inline comments explaining WHY, not WHAT?
- [ ] Are there any obvious comments that should be deleted?
- [ ] Are business rules documented so a new dev understands without asking?

---

## How to View Javadoc in STS

**Hover tooltip** — hover over any method name → Javadoc popup appears instantly.

**Generate HTML docs:**
Right click project → `Export` → `Java` → `Javadoc` → set output folder → `Finish`
Opens `index.html` — looks exactly like official Spring/Java documentation sites.

---

## Generate Javadoc via Maven (Recommended)

**Command — run in project root:**
```bash
mvn javadoc:javadoc
```

**View generated docs — open in browser:**
```
d:\LMS\Livekit\lms-live-service\target\reports\apidocs\index.html
```

Or paste directly in browser address bar:
```
file:///D:/LMS/Livekit/lms-live-service/target/reports/apidocs/index.html
```

**If build fails with warnings — add this to pom.xml to allow warnings:**
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
| `failOnWarnings=false` | Missing `@param`/`@return` warnings don't break the build |
| `doclint=none` | Disables strict HTML validation inside Javadoc comments |

**Common error — `semicolon missing` in Javadoc:**
Caused by special characters like `(e.g. Q&A)` — Javadoc parser treats `&` as HTML entity start.
Fix: remove parentheses or rewrite as plain text — `for Q&A sessions` instead of `(e.g. Q&A)`.

---

*Good comments are a gift to your future self and your teammates.*

---

## How to Read Documentation & Debug SDK Errors

### The Order to Follow

```
Error hits
    ↓
Step 1 — Read the error message carefully
    ↓
Step 2 — Check the actual source (jar or GitHub)
    ↓
Step 3 — Official docs
    ↓
Step 4 — Maven Central for correct version/artifact
    ↓
Step 5 — Google / StackOverflow (last resort)
```

---

### Step 1 — Read the Error First

When we got `Cannot instantiate VideoGrant`:
```
Cannot instantiate the type VideoGrant
```
This tells you exactly — `VideoGrant` cannot be `new`-ed. That means it's either:
- An **interface**
- An **abstract class**
- A **sealed class**

The error itself told the answer before any Googling. Train yourself to extract meaning from error messages — they are precise.

---

### Step 2 — Read the Jar Source (Most Reliable)

When docs are wrong or outdated, the **source code never lies**.

```bash
# Extract sources jar from your .m2 folder
jar xf livekit-server-0.12.1-sources.jar

# Read the actual class
Get-Content io/livekit/server/VideoGrant.kt
```

From that we saw:
```kotlin
sealed class VideoGrant(val key: String, val value: Any)
class RoomJoin(value: Boolean) : VideoGrant("roomJoin", value)
class CanPublish(value: Boolean) : VideoGrant("canPublish", value)
```

Sealed class = cannot instantiate directly. Each permission is its own subclass.
That's ground truth — no blog or AI can be more accurate than the actual source.

**In STS — easier way:**
- Hold `Ctrl` + click any class name → jumps to source directly
- Right click jar in Maven Dependencies → `Properties` → attach sources jar
- Now every `Ctrl+click` shows you the real implementation

---

### Step 3 — Official Docs

**How to read official docs efficiently — don't read top to bottom:**

```
1. Know what you want to do  →  "create a room in LiveKit"
2. Search that exact phrase in docs
3. Find the method signature
4. Read @param and @return only
5. Copy the minimal example
6. Run it, fix errors from error message
```

| SDK | Where to look |
|-----|--------------|
| LiveKit Java | `docs.livekit.io` for concepts, GitHub README for Java examples |
| Spring Boot | `docs.spring.io` — excellent, always up to date |
| Any SDK | GitHub repo → README → look for Java/usage section |

---

### Step 4 — Maven Central for Artifact Names & Versions

When artifact not found error appears — never trust a blog for artifact names:

```
Go to: central.sonatype.com
Search: io.livekit livekit-server
Check: what artifact IDs and versions actually exist
```

This is exactly how we caught:
- `livekit-server-sdk` → wrong, does not exist
- `livekit-server` → correct
- version `0.10.1` → does not exist
- version `0.12.1` → correct, latest

**Always verify on Maven Central before adding any dependency.**

---

### Step 5 — Google / StackOverflow

Last resort. Useful for:
- Common Spring Boot config issues
- Generic Java errors
- "How to do X in framework Y"

Not useful for:
- Specific SDK version APIs — answers are outdated
- Artifact names — blogs copy each other's mistakes

---

## Mental Model — What to Build vs What to Read

```
You want to: Generate a LiveKit token
                ↓
Ask: What class does this?       → AccessToken
                ↓
Ask: What methods does it have?  → Ctrl+click in STS or read source jar
                ↓
Ask: What parameters?            → Read @param in Javadoc or source
                ↓
Write the code → run it → fix errors from error message
```

You don't need to read the entire documentation.
You need to know 4 things:
1. **What class** handles what I want
2. **What method** on that class
3. **What parameters** it needs
4. **What it returns**

Everything else is noise until you need it.

---

## Quick Reference — Where to Look for What

| Problem | Where to look |
|---------|--------------|
| Wrong artifact name | Maven Central — `central.sonatype.com` |
| Method doesn't exist / wrong API | Source jar — `Ctrl+click` in STS |
| How to configure Spring | `docs.spring.io` |
| SDK usage example | GitHub README of that SDK |
| Generic Java error | StackOverflow |
| Cannot instantiate error | Read the class — interface/abstract/sealed |
| Version compatibility | Maven Central release notes or GitHub releases |

---

*Read errors carefully. Check source first. Docs second. Google last.*

---

## Naming Conventions — Classes, Files & Packages

### Pattern: `[Domain] + [Layer]`

Every file name should answer two questions: **what domain** + **what layer**.

```
LiveSession   + Controller  = LiveSessionController
LiveKit       + Config      = LiveKitConfig
LiveSession   + Service     = LiveSessionService
LiveSession   + Repository  = LiveSessionRepository
LiveKit       + Webhook     + Controller = LiveKitWebhookController
```

New developer joins → reads file name → knows exactly what's inside without opening it.

### Why not just `WebhookController`?

In a real project you'll have multiple webhooks:

```
LiveKitWebhookController    ← LiveKit room events
PaymentWebhookController    ← Razorpay/Stripe payment events
SmsWebhookController        ← SMS delivery status
```

`WebhookController` alone tells you nothing. `LiveKitWebhookController` is precise.

### Package Naming — Same Rule

```
controller/   → HTTP entry points called by frontend
webhook/      → callbacks from external systems (LiveKit, Razorpay)
service/      → business logic
repository/   → DB access
config/       → Spring beans and configuration
dto/          → request/response objects
model/        → JPA entities
```

Webhook gets its own package because it is a different kind of controller —
not called by your frontend, called by an external system.
Keeping it separate makes that architectural difference visible.
