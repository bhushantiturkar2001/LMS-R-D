# Personal Roadmap — System Design & Interview Prep
**Bhushan | Started: May 2026**

---

## What This LMS Project Already Teaches You

These are concepts you are building hands-on — not just reading.

| # | Concept | Where in LMS |
|---|---------|-------------|
| 1 | **Microservices Architecture** | Every service is independent — Live, Auth, Payment, Notification |
| 2 | **API Gateway Pattern** | Spring Cloud Gateway — single entry, JWT + RBAC |
| 3 | **Sync vs Async Communication** | REST for token/join, Kafka for recording/notifications |
| 4 | **Event-Driven Architecture** | LiveKit webhook → Kafka → multiple consumers |
| 5 | **Fan-out Pattern** | 1 Kafka event → Email + SMS + Audit + Finance all consume |
| 6 | **Webhook Pattern** | LiveKit calls your backend on room events |
| 7 | **Token-based Auth (JWT)** | Every request validated at gateway level |
| 8 | **RBAC** | Student vs Instructor vs Admin — permission layers |
| 9 | **SFU / WebRTC Scaling** | 1 instructor → 500 students via LiveKit SFU |
| 10 | **Load Balancing** | Multiple LiveKit instances behind Nginx/ALB |
| 11 | **Polyglot Persistence** | PostgreSQL + MongoDB + Redis + S3 — right DB for right job |
| 12 | **Database per Service** | Each microservice owns its own data |
| 13 | **Caching Strategy (Redis)** | Sessions, room state, fast lookups |
| 14 | **Capacity Planning** | 540p × 500 users per 1Gbps — real math |
| 15 | **Centralized Logging** | ELK Stack — Log Collector service |

> This project alone covers ~70% of what FAANG system design interviews ask.

---

## What You Still Need to Cover (External Study)

These are NOT in the LMS project but are asked in Google/Amazon/Flipkart interviews.

### Must Know Concepts

| # | Topic | Why Important |
|---|-------|--------------|
| 1 | **Consistent Hashing** | How distributed caches/DBs route requests — asked in every senior interview |
| 2 | **CAP Theorem** | CP vs AP systems — why you pick PostgreSQL vs Cassandra |
| 3 | **Database Sharding** | How to split DB when one server can't handle data |
| 4 | **Database Replication** | Master-slave, read replicas — high availability |
| 5 | **Rate Limiting** | Token bucket / leaky bucket — API protection |
| 6 | **CDN** | How Netflix/YouTube serve video globally fast |
| 7 | **Bloom Filter** | Check if item exists without loading full DB |
| 8 | **Distributed Transactions (SAGA)** | Payment across microservices — how to handle failure |
| 9 | **Circuit Breaker** | Service A fails → don't cascade to Service B |
| 10 | **Service Discovery** | How microservices find each other (Eureka/Consul) |
| 11 | **Idempotency** | Same request sent twice → same result, no duplicate payment |
| 12 | **Long Polling vs SSE vs WebSocket** | When to use which for real-time |
| 13 | **Message Queue vs Event Streaming** | RabbitMQ vs Kafka — difference and when |
| 14 | **Distributed Locking** | Two servers trying to do same thing — Redis SETNX |
| 15 | **Two-Phase Commit (2PC)** | Distributed DB consistency — why it's slow |

---

## HLD — High Level Design

> HLD = Big picture. No code. Boxes and arrows. Interviewer wants to see your thinking.

### What Interviewer Asks in HLD

- "Design YouTube" / "Design WhatsApp" / "Design Uber"
- You have 45 minutes — draw the system

### HLD Framework (Use this every time)

```
Step 1 — Clarify Requirements (5 min)
  → Functional: what the system does
  → Non-functional: scale, latency, availability

Step 2 — Estimate Scale (3 min)
  → DAU (Daily Active Users)
  → Reads vs Writes ratio
  → Storage needed

Step 3 — API Design (5 min)
  → What endpoints exist
  → Request/Response shape

Step 4 — Database Design (5 min)
  → SQL vs NoSQL — why
  → Schema sketch

Step 5 — High Level Diagram (15 min)
  → Client → API Gateway → Services → DB
  → Async flows via Kafka
  → Cache layer

Step 6 — Deep Dive on bottlenecks (10 min)
  → Where will it fail at scale
  → How to fix it
```

### HLD Topics to Practice (Classic Problems)

| Problem | Key Concepts Tested |
|---------|-------------------|
| Design YouTube | CDN, video encoding, storage, recommendation |
| Design WhatsApp | WebSocket, message queue, delivery receipts |
| Design Uber | Geo-indexing, real-time location, matching |
| Design Twitter Feed | Fan-out, cache, timeline generation |
| Design URL Shortener | Hashing, redirect, analytics |
| Design Rate Limiter | Token bucket, Redis, distributed counter |
| Design Notification System | Kafka, push/email/SMS, fan-out |
| **Design Live Class System** | SFU, WebRTC, token, recording — **you already built this** |

---

## LLD — Low Level Design

> LLD = Code level design. Class diagrams, design patterns, SOLID principles.

### What Interviewer Asks in LLD

- "Design a Parking Lot" / "Design Chess" / "Design BookMyShow"
- Write classes, interfaces, relationships

### LLD Framework (Use this every time)

```
Step 1 — Identify Entities (nouns)
  → Parking Lot has: ParkingLot, Floor, Slot, Vehicle, Ticket

Step 2 — Identify Behaviors (verbs)
  → park(), unpark(), findSlot(), generateTicket()

Step 3 — Apply Design Patterns
  → Factory, Strategy, Observer, Singleton

Step 4 — Write Class Diagram
  → Relationships: has-a, is-a, uses

Step 5 — Write key classes in code
  → Interfaces first, then implementations
```

### Design Patterns You Must Know

| Pattern | Real Use in LMS |
|---------|----------------|
| **Singleton** | `RoomServiceClient` bean — one instance shared |
| **Factory** | Create different token types (student/instructor) |
| **Strategy** | Different recording strategies (composite/track/RTMP) |
| **Observer** | Webhook events → multiple handlers react |
| **Builder** | Building complex request objects |
| **Decorator** | Adding auth/logging to API calls |
| **Chain of Responsibility** | API Gateway filters (JWT → RBAC → Route) |

### LLD Topics to Practice

| Problem | Key Patterns |
|---------|-------------|
| Parking Lot | Factory, Singleton, Strategy |
| BookMyShow | Observer, Factory, Composite |
| Chess Game | Strategy, Command, State |
| ATM Machine | State, Command, Singleton |
| Elevator System | State, Strategy, Observer |
| **Live Session Service** | Singleton, Factory, Observer — **you already built this** |

---

## SOLID Principles (Must Know for LLD)

| Principle | One Line | Example in LMS |
|-----------|----------|---------------|
| **S** — Single Responsibility | One class, one job | `LiveKitTokenService` only generates tokens |
| **O** — Open/Closed | Open for extension, closed for modification | Add new recording type without changing existing |
| **L** — Liskov Substitution | Subclass should replace parent safely | Student/Instructor both extend Participant |
| **I** — Interface Segregation | Don't force unused methods | Separate `TokenService` and `RoomService` interfaces |
| **D** — Dependency Inversion | Depend on abstraction, not implementation | `@Autowired RoomServiceClient` not `new RoomServiceClient()` |

---

## Interview Cheat Sheet

### When asked "How would you scale this?"
1. Add caching (Redis) for read-heavy data
2. Add read replicas for DB
3. Shard DB if single table too large
4. Add CDN for static/video content
5. Use async (Kafka) for non-critical operations
6. Horizontal scaling behind load balancer

### When asked "How do you handle failures?"
1. Retry with exponential backoff
2. Circuit breaker (Resilience4j)
3. Dead letter queue for failed Kafka messages
4. Idempotency keys for payments
5. Saga pattern for distributed transactions

### Numbers to Remember
| Thing | Number |
|-------|--------|
| Read from RAM | ~100 ns |
| Read from SSD | ~100 µs |
| Network round trip | ~1 ms |
| Read from HDD | ~10 ms |
| 1 Gbps bandwidth | ~500 users at 540p video |
| Redis throughput | ~100k ops/sec |
| Kafka throughput | ~1M messages/sec |

---

## Study Order Recommendation

```
Phase 1 — Finish LMS Project (Current)
  → You get hands-on with 15 system design concepts

Phase 2 — Fill Gaps (1-2 months)
  → CAP Theorem, Consistent Hashing, Sharding
  → SAGA pattern, Circuit Breaker, Rate Limiting
  → Read: "Designing Data-Intensive Applications" (DDIA) — best book

Phase 3 — HLD Practice (1 month)
  → Solve 8-10 classic HLD problems
  → Draw diagrams, time yourself (45 min per problem)

Phase 4 — LLD Practice (1 month)
  → Solve 5-6 classic LLD problems
  → Write actual code, not just diagrams

Phase 5 — Mock Interviews
  → Practice with a peer or on Pramp/Interviewing.io
```

---

*This roadmap is based on actual LMS project + FAANG interview patterns*
*Updated: May 2026*
