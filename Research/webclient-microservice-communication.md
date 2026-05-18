# WebClient — Microservice Communication
**Bhushan | May 2026**

---

## What is WebClient

`WebClient` is Spring's HTTP client for calling other microservices.
It replaced the old `RestTemplate` (still works but deprecated for new projects).

| | RestTemplate (old) | WebClient (new) |
|--|-------------------|----------------|
| Style | Blocking — thread waits | Non-blocking — reactive |
| Spring version | All versions | Spring 5+ |
| Recommended | ❌ Deprecated | ✅ Use this |
| Works in non-reactive app | ✅ Yes (with `.block()`) | ✅ Yes |

> You don't need to build a full reactive app to use WebClient.
> In a normal Spring Boot app, just call `.block()` at the end to get the result synchronously.

---

## Scenario We Are Building

```
Live Session Service (port 8084)
        ↓ calls via WebClient
Student Service (port 8081)
        ↓ checks
"Is this student enrolled in this course?"
```

Before generating a token, Live Session Service must verify enrollment.
Instead of duplicating enrollment logic, it calls Student Service via HTTP.

---

## Step 1 — Create Two Spring Boot Projects

### Project 1 — Student Service
- **Artifact:** `student-service`
- **Port:** `8081`
- **Responsibility:** Manage students, enrollments

### Project 2 — Live Session Service
- **Artifact:** `lms-live-service`
- **Port:** `8084`
- **Responsibility:** Live classes, token generation

In Spring Initializr (`start.spring.io`) for both projects select:
- Spring Web
- Spring Boot DevTools
- Lombok

---

## Step 2 — Add WebClient Dependency

WebClient comes from `spring-boot-starter-webflux`. Add to the **calling service** (`lms-live-service`):

```xml
<!-- WebClient — for calling other microservices -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

> You only add this to the service that **makes** the HTTP call.
> The service being called (`student-service`) needs no special dependency — it's just a normal REST API.

---

## Step 3 — Configure Ports

**student-service → `application.yml`:**
```yaml
spring:
  application:
    name: student-service
server:
  port: 8081
```

**lms-live-service → `application.yml`:**
```yaml
spring:
  application:
    name: lms-live-service
server:
  port: 8084

services:
  student-service:
    url: http://localhost:8081   # URL of student service
```

> Store service URLs in yml — never hardcode in Java classes.
> In production, replace `localhost` with actual service name or load balancer URL.

---

## Step 4 — Create the Endpoint in Student Service

Student Service exposes an enrollment check endpoint:

```java
// StudentController.java in student-service
@RestController
@RequestMapping("/api/students")
public class StudentController {

    /**
     * Checks if a student is enrolled in a specific course.
     * Called by Live Session Service before generating join token.
     *
     * @param studentId the student to check
     * @param courseId  the course to check enrollment for
     * @return true if enrolled, false otherwise
     */
    @GetMapping("/{studentId}/enrolled/{courseId}")
    public ResponseEntity<Boolean> isEnrolled(
            @PathVariable String studentId,
            @PathVariable String courseId) {

        // Hardcoded for now — replace with real DB check
        boolean enrolled = true;
        return ResponseEntity.ok(enrolled);
    }
}
```

**Test it directly:**
```
GET http://localhost:8081/api/students/student-1/enrolled/physics-101
Response: true
```

---

## Step 5 — Create WebClient Bean in Live Session Service

Create a `WebClientConfig.java` in the `config` package:

```java
// WebClientConfig.java
@Configuration
public class WebClientConfig {

    @Value("${services.student-service.url}")
    private String studentServiceUrl;

    /**
     * WebClient bean configured for Student Service.
     * Base URL injected from application.yml — never hardcoded.
     */
    @Bean
    public WebClient studentServiceClient() {
        return WebClient.builder()
                .baseUrl(studentServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
```

> `baseUrl` sets the common prefix — all calls from this client start with `http://localhost:8081`.
> You can create multiple `WebClient` beans — one per downstream service.

---

## Step 6 — Create a Client Class

Create `StudentServiceClient.java` in the `service` package:

```java
// StudentServiceClient.java
@Service
public class StudentServiceClient {

    private final WebClient studentServiceClient;

    /**
     * Injects the WebClient bean configured for Student Service.
     * Constructor injection preferred over @Autowired for testability.
     */
    public StudentServiceClient(WebClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    /**
     * Calls Student Service to verify enrollment.
     * Uses .block() to make the reactive call synchronous —
     * acceptable in non-reactive Spring MVC apps.
     *
     * @param studentId the student to check
     * @param courseId  the course to verify enrollment for
     * @return true if enrolled, false if not enrolled or service unreachable
     */
    public boolean isEnrolled(String studentId, String courseId) {
        try {
            Boolean result = studentServiceClient
                    .get()
                    .uri("/api/students/{studentId}/enrolled/{courseId}", studentId, courseId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();  // block() makes async call synchronous

            return Boolean.TRUE.equals(result);

        } catch (Exception e) {
            // If Student Service is down, fail safe — deny access
            // In production: add circuit breaker here (Resilience4j)
            return false;
        }
    }
}
```

---

## Step 7 — Use It in Live Session Service

Now use `StudentServiceClient` in `LiveSessionController`:

```java
// LiveSessionController.java
@RestController
@RequestMapping("/api/live")
public class LiveSessionController {

    @Autowired
    private LiveSessionService liveSessionService;

    @Autowired
    private StudentServiceClient studentServiceClient;  // inject the client

    /**
     * Student joins a live class.
     * Verifies enrollment via Student Service before issuing token.
     */
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> joinSession(@RequestBody JoinRequest req) {

        // Call Student Service to verify enrollment
        boolean enrolled = studentServiceClient.isEnrolled(req.getStudentId(), req.getCourseId());
        if (!enrolled) {
            return ResponseEntity.status(403).build();  // Forbidden — not enrolled
        }

        try {
            JoinResponse response = liveSessionService.joinSession(
                req.getRoomName(), req.getStudentId(), req.getStudentName()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

---

## Step 8 — Test the Full Flow

**Start both services:**
```bash
# Terminal 1 — Student Service
cd student-service
mvn spring-boot:run

# Terminal 2 — Live Session Service
cd lms-live-service
mvn spring-boot:run
```

**Test via Postman:**
```
POST http://localhost:8084/api/live/join
Content-Type: application/json

{
  "roomName": "physics-101-abc123",
  "studentId": "student-1",
  "studentName": "Rahul",
  "courseId": "physics-101"
}
```

**What happens internally:**
```
Postman → Live Session Service (8084)
    ↓ WebClient GET
Student Service (8081) /api/students/student-1/enrolled/physics-101
    ↓ returns true
Live Session Service → generates token → returns JoinResponse
    ↓
Postman receives token ✅
```

---

## WebClient Methods Reference

| Method | Use |
|--------|-----|
| `.get()` | HTTP GET request |
| `.post()` | HTTP POST request |
| `.put()` | HTTP PUT request |
| `.delete()` | HTTP DELETE request |
| `.uri("/path/{var}", value)` | URL with path variable |
| `.bodyValue(object)` | Send request body (POST/PUT) |
| `.retrieve()` | Execute and get response |
| `.bodyToMono(Type.class)` | Parse response to single object |
| `.bodyToFlux(Type.class)` | Parse response to list/stream |
| `.block()` | Make async call synchronous |
| `.onStatus(...)` | Handle specific HTTP status codes |

---

## Handling Errors Properly

```java
Boolean result = studentServiceClient
        .get()
        .uri("/api/students/{studentId}/enrolled/{courseId}", studentId, courseId)
        .retrieve()
        .onStatus(
            status -> status.is4xxClientError(),
            response -> Mono.error(new RuntimeException("Student not found"))
        )
        .onStatus(
            status -> status.is5xxServerError(),
            response -> Mono.error(new RuntimeException("Student Service unavailable"))
        )
        .bodyToMono(Boolean.class)
        .block();
```

---

## POST Request Example

Calling another service with a request body:

```java
// Sending a request body via WebClient POST
SessionCreatedEvent event = new SessionCreatedEvent(roomName, instructorId, courseId);

notificationServiceClient
        .post()
        .uri("/api/notifications/session-started")
        .bodyValue(event)
        .retrieve()
        .bodyToMono(Void.class)
        .block();
```

---

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Hardcoding service URL in Java | Store in `application.yml`, inject with `@Value` |
| Not handling service-down scenario | Wrap in try-catch, return safe default |
| Using `RestTemplate` in new code | Use `WebClient` — RestTemplate is deprecated |
| Creating `new WebClient()` everywhere | Create one `@Bean`, inject it |
| Forgetting `.block()` in MVC app | Without it, call never executes |
| Passing `ws://` URL to WebClient | WebClient only accepts `http://` or `https://` |

---

## Service Communication Patterns Summary

| Pattern | When to use | Technology |
|---------|------------|-----------|
| **Sync REST (WebClient)** | Need immediate response — token, enrollment check | WebClient |
| **Async Event** | Fire and forget — notifications, audit, recording | Kafka |
| **Sync gRPC** | High performance internal calls, strict contracts | gRPC (future) |

> Rule of thumb:
> - User is waiting for response → **WebClient (sync)**
> - Background processing, notifications → **Kafka (async)**

---

*WebClient is the standard way to do sync microservice communication in Spring Boot.*

---

## Production Checklist — What to Follow in Real Projects

### 1. Error Handling

| Scenario | What to do |
|----------|-----------|
| Called service returns 4xx | Throw meaningful exception — `StudentNotFoundException` |
| Called service returns 5xx | Throw `ServiceUnavailableException`, trigger fallback |
| Network timeout | Set timeout on WebClient, return fallback response |
| Unexpected exception | Log with full context, never swallow silently |

```java
// Never do this — swallows the error silently
try {
    return client.get()...block();
} catch (Exception e) {
    return null;  // ❌ caller has no idea what went wrong
}

// Do this — meaningful fallback with logging
} catch (Exception e) {
    log.error("Student Service call failed for studentId={}, courseId={}: {}",
        studentId, courseId, e.getMessage());
    return false;  // ✅ safe default + logged
}
```

---

### 2. Timeout Configuration

Never let a slow service hang your thread forever. Always set timeouts:

```java
@Bean
public WebClient studentServiceClient() {
    HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(3));   // 3 sec max wait

    return WebClient.builder()
            .baseUrl(studentServiceUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
}
```

> Rule: set timeout lower than your own API's SLA.
> If your API must respond in 5 sec, set downstream timeout to 3 sec.

---

### 3. Circuit Breaker (Resilience4j)

If Student Service is down, don't keep hammering it. Open the circuit after N failures:

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

```java
// Annotate the method — auto opens circuit after failures
@CircuitBreaker(name = "studentService", fallbackMethod = "enrollmentFallback")
public boolean isEnrolled(String studentId, String courseId) {
    return client.get()...block();
}

// Fallback — called when circuit is open
public boolean enrollmentFallback(String studentId, String courseId, Exception e) {
    log.warn("Circuit open for Student Service — denying access for safety");
    return false;  // fail safe
}
```

```yaml
# application.yml — circuit breaker config
resilience4j:
  circuitbreaker:
    instances:
      studentService:
        slidingWindowSize: 10          # track last 10 calls
        failureRateThreshold: 50       # open if 50% fail
        waitDurationInOpenState: 10s   # wait 10s before retry
```

---

### 4. Retry Logic

Transient failures (network blip) should be retried automatically:

```java
@Retry(name = "studentService", fallbackMethod = "enrollmentFallback")
@CircuitBreaker(name = "studentService", fallbackMethod = "enrollmentFallback")
public boolean isEnrolled(String studentId, String courseId) {
    return client.get()...block();
}
```

```yaml
resilience4j:
  retry:
    instances:
      studentService:
        maxAttempts: 3                  # try 3 times
        waitDuration: 500ms             # wait 500ms between retries
        retryExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
```

> Retry + Circuit Breaker together = standard production pattern.
> Retry handles transient failures. Circuit Breaker handles persistent failures.

---

### 5. Logging — What to Log

```java
public boolean isEnrolled(String studentId, String courseId) {

    log.info("Checking enrollment — studentId={}, courseId={}", studentId, courseId);

    try {
        Boolean result = client.get()
                .uri("/api/students/{s}/enrolled/{c}", studentId, courseId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        log.info("Enrollment result — studentId={}, courseId={}, enrolled={}",
                studentId, courseId, result);

        return Boolean.TRUE.equals(result);

    } catch (Exception e) {
        log.error("Enrollment check failed — studentId={}, courseId={}, error={}",
                studentId, courseId, e.getMessage());
        return false;
    }
}
```

| Log Level | When to use |
|-----------|------------|
| `log.info` | Normal flow — request received, result returned |
| `log.warn` | Degraded but working — fallback triggered, circuit open |
| `log.error` | Something broke — exception, service down |
| `log.debug` | Detailed trace — only in dev, never in prod |

> Never log sensitive data — passwords, tokens, card numbers, personal info.

---

### 6. Meaningful Exceptions

Don't throw generic `RuntimeException`. Create specific ones:

```java
// Create custom exceptions
public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(String studentId) {
        super("Student not found: " + studentId);
    }
}

public class StudentServiceException extends RuntimeException {
    public StudentServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Use them
.onStatus(status -> status.value() == 404,
    response -> Mono.error(new StudentNotFoundException(studentId)))
.onStatus(HttpStatusCode::is5xxServerError,
    response -> Mono.error(new StudentServiceException("Student Service unavailable", null)))
```

---

### 7. Global Exception Handler

Handle all exceptions in one place — don't repeat try-catch in every controller:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<String> handleStudentNotFound(StudentNotFoundException e) {
        log.warn("Student not found: {}", e.getMessage());
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(StudentServiceException.class)
    public ResponseEntity<String> handleServiceDown(StudentServiceException e) {
        log.error("Downstream service error: {}", e.getMessage());
        return ResponseEntity.status(503).body("Service temporarily unavailable");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception e) {
        log.error("Unexpected error: {}", e.getMessage());
        return ResponseEntity.status(500).body("Internal server error");
    }
}
```

---

### 8. Never Hardcode URLs or Secrets

```java
// ❌ Never do this
WebClient.builder().baseUrl("http://localhost:8081").build();

// ✅ Always do this
@Value("${services.student-service.url}")
private String studentServiceUrl;
WebClient.builder().baseUrl(studentServiceUrl).build();
```

In production, service URLs come from:
- Environment variables
- Kubernetes ConfigMaps
- Service discovery (Eureka/Consul) — service name instead of IP

---

### 9. Idempotency for POST Calls

If a POST call is retried (network blip), it should not create duplicate records:

```java
// Send a unique request ID — server uses it to detect duplicates
webClient.post()
        .uri("/api/enrollments")
        .header("X-Request-Id", UUID.randomUUID().toString())
        .bodyValue(enrollmentRequest)
        .retrieve()
        .bodyToMono(Void.class)
        .block();
```

Server checks: "Have I already processed this `X-Request-Id`?" → if yes, return same response without re-processing.

---

### 10. Health Checks

Every service should expose a health endpoint so load balancers and monitoring tools know it's alive:

```yaml
# application.yml — Spring Actuator
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
  endpoint:
    health:
      show-details: always
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```
GET http://localhost:8081/actuator/health
Response: { "status": "UP" }
```

---

### Production Checklist Summary

| # | Rule | Why |
|---|------|-----|
| 1 | Handle all HTTP error codes explicitly | Know exactly what went wrong |
| 2 | Always set timeouts on WebClient | Prevent thread starvation |
| 3 | Add Circuit Breaker (Resilience4j) | Stop hammering a dead service |
| 4 | Add Retry for transient failures | Handle network blips automatically |
| 5 | Log with context — IDs, not just messages | Trace issues in production logs |
| 6 | Use meaningful custom exceptions | Clear error propagation |
| 7 | Global exception handler | Single place for all error responses |
| 8 | Never hardcode URLs or secrets | Config changes without code deploy |
| 9 | Idempotency keys on POST calls | Safe retries, no duplicate data |
| 10 | Expose health endpoints (Actuator) | Load balancer and monitoring |
| 11 | Javadoc on every public method | Future devs understand intent |
| 12 | Validate input before calling downstream | Don't waste a network call on bad data |

---

## Scalability Mindset — How to Write Code That Handles 1000x Load

> The difference between a junior and senior developer is not syntax.
> It's what they think about **before** writing the first line.

---

### The Core Question to Ask Before Every Feature

```
"What happens to this code when 1000 users hit it at the same time?"
```

If you can't answer that — the code is not production ready.

---

### 1. Never Trust a Single Thread to Do Everything

**Junior thinking:**
```java
// Works fine for 10 users
public JoinResponse joinSession(String roomName, String studentId) {
    checkEnrollment();      // calls Student Service — 200ms
    checkRoomActive();      // DB query — 50ms
    generateToken();        // CPU — 10ms
    saveAttendance();       // DB write — 50ms
    sendWelcomeEmail();     // calls Email Service — 300ms
    return token;
}
// Total: 610ms per request, 1 thread blocked the whole time
```

**Scalable thinking:**
```java
// Do only what the user is waiting for — everything else async
public JoinResponse joinSession(String roomName, String studentId) {
    checkEnrollment();      // SYNC — user needs this answer now
    checkRoomActive();      // SYNC — user needs this answer now
    generateToken();        // SYNC — user needs this answer now

    // Fire and forget — user doesn't need to wait for these
    kafkaTemplate.send("attendance-events", new AttendanceEvent(...));  // ASYNC
    kafkaTemplate.send("email-events", new WelcomeEmailEvent(...));     // ASYNC

    return token;           // respond in ~260ms instead of 610ms
}
```

> Rule: **Sync only what the user is waiting for. Async everything else.**

---

### 2. Stateless Services

**Wrong — state stored in memory:**
```java
@Service
public class LiveSessionService {
    // ❌ Stored in memory — dies when service restarts
    // ❌ If 3 instances run, each has different data
    private Map<String, String> activeRooms = new HashMap<>();
}
```

**Scalable — state stored externally:**
```java
@Service
public class LiveSessionService {
    @Autowired
    private LiveSessionRepository sessionRepository;  // ✅ DB — survives restart

    @Autowired
    private RedisTemplate<String, String> redisTemplate; // ✅ Redis — shared across instances
}
```

> Rule: **Any instance of your service should be able to handle any request.**
> If you need 3 instances to handle load, all 3 must see the same state.

---

### 3. Database — Think Before Every Query

**Wrong — N+1 query problem:**
```java
// Fetches 100 students, then runs 100 separate queries for each enrollment
List<Student> students = studentRepo.findAll();
for (Student s : students) {
    List<Enrollment> enrollments = enrollmentRepo.findByStudentId(s.getId()); // ❌ 100 queries
}
```

**Scalable — one query with join:**
```java
// One query fetches everything
List<StudentWithEnrollments> result = studentRepo.findAllWithEnrollments(); // ✅ 1 query
```

**Other DB rules:**
- Always add indexes on columns used in `WHERE`, `JOIN`, `ORDER BY`
- Never do `SELECT *` — fetch only columns you need
- Use pagination — never return 10,000 rows at once
- Use connection pooling (HikariCP — Spring Boot default) — don't create new DB connection per request

---

### 4. Caching — Don't Hit the DB for the Same Data Twice

```java
// Without cache — DB hit every time
public boolean isEnrolled(String studentId, String courseId) {
    return enrollmentRepo.existsByStudentIdAndCourseId(studentId, courseId); // DB every time
}

// With Redis cache — DB hit only once per student per course
@Cacheable(value = "enrollments", key = "#studentId + ':' + #courseId")
public boolean isEnrolled(String studentId, String courseId) {
    return enrollmentRepo.existsByStudentIdAndCourseId(studentId, courseId);
}
// Second call for same student+course → served from Redis, no DB hit
```

**What to cache:**
- Enrollment status — doesn't change often
- Course details — rarely updated
- User profile — rarely updated

**What NOT to cache:**
- Live session status — changes every second
- Payment data — must always be fresh
- Anything that changes frequently

---

### 5. Connection Pooling — Don't Create New Connections Per Request

Every DB connection takes ~50ms to create. With 1000 users, that's 50 seconds wasted.

```yaml
# application.yml — HikariCP (Spring Boot default pool)
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # max 20 DB connections
      minimum-idle: 5            # keep 5 ready at all times
      connection-timeout: 3000   # fail fast if no connection in 3s
      idle-timeout: 600000       # close idle connections after 10 min
```

> Same applies to WebClient — reuse the same `WebClient` bean, never create `new WebClient()` per request.

---

### 6. Pagination — Never Return All Records

```java
// ❌ Returns all 100,000 sessions — kills DB and memory
List<LiveSession> sessions = sessionRepo.findAll();

// ✅ Returns 20 at a time — predictable load
Page<LiveSession> sessions = sessionRepo.findAll(PageRequest.of(page, 20));
```

> Rule: Every list endpoint must have a page size limit.
> Default page size: 20. Max allowed: 100. Never unlimited.

---

### 7. Async for Heavy Operations

```java
// ❌ User waits 30 seconds for video to process
public void endSession(String roomName) {
    deleteRoom(roomName);
    processRecording(roomName);   // 30 seconds — user is blocked
    sendEmailToAllStudents();     // 10 seconds — user is still blocked
}

// ✅ User gets response in 100ms, heavy work happens in background
public void endSession(String roomName) {
    deleteRoom(roomName);
    kafkaTemplate.send("session-ended", new SessionEndedEvent(roomName)); // fire and forget
    // Recording + emails handled by consumers asynchronously
}
```

---

### 8. Fail Fast — Validate Early

```java
// ❌ Calls DB, calls Student Service, generates token... then fails on null name
public JoinResponse joinSession(JoinRequest req) {
    boolean enrolled = studentServiceClient.isEnrolled(req.getStudentId(), req.getCourseId());
    String token = tokenService.generate(req.getRoomName(), req.getStudentId(), req.getStudentName());
    // studentName was null — NullPointerException after wasting 2 network calls
}

// ✅ Validate first — fail before wasting any resources
public JoinResponse joinSession(JoinRequest req) {
    if (req.getStudentId() == null || req.getStudentId().isBlank())
        throw new IllegalArgumentException("studentId is required");
    if (req.getRoomName() == null || req.getRoomName().isBlank())
        throw new IllegalArgumentException("roomName is required");

    // Now safe to make expensive calls
    boolean enrolled = studentServiceClient.isEnrolled(...);
}
```

---

### 9. Rate Limiting — Protect Your Service

Without rate limiting, one bad client can send 10,000 requests/sec and take down your service.

```yaml
# application.yml — Resilience4j rate limiter
resilience4j:
  ratelimiter:
    instances:
      joinSession:
        limitForPeriod: 100          # max 100 requests
        limitRefreshPeriod: 1s       # per second
        timeoutDuration: 0           # reject immediately if limit hit
```

```java
@RateLimiter(name = "joinSession")
@PostMapping("/join")
public ResponseEntity<JoinResponse> joinSession(@RequestBody JoinRequest req) {
```

---

### 10. Think in Terms of Load — Before Writing Code

Before writing any feature, ask these questions:

| Question | Why it matters |
|----------|---------------|
| How many users will call this per second? | Determines if you need cache, async, or queue |
| Does this query run on every request? | If yes — add index or cache |
| Is this operation idempotent? | If not — add idempotency key |
| What happens if the downstream service is slow? | Add timeout + circuit breaker |
| What happens if this service crashes mid-operation? | Design for partial failure |
| Can this be done async instead of sync? | If user doesn't need to wait — make it async |
| What's the worst case data size? | Never assume small — design for large |

---

### Scalability Mindset Summary

| Mindset | What it means in code |
|---------|----------------------|
| **Stateless** | No in-memory state — use DB or Redis |
| **Async by default** | Only block the thread when user is waiting |
| **Cache aggressively** | Don't hit DB for data that rarely changes |
| **Fail fast** | Validate input before expensive operations |
| **Paginate everything** | Never return unbounded lists |
| **Pool connections** | Reuse DB and HTTP connections |
| **Rate limit** | Protect your service from abuse |
| **Design for failure** | Every downstream call can fail — handle it |
| **Measure before optimizing** | Don't guess bottlenecks — use metrics |

> Google engineers don't write faster code.
> They write code that **doesn't do unnecessary work** at scale.
