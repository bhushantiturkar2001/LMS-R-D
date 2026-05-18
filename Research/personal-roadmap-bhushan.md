# Personal Roadmap — System Design & Interview Prep
**Bhushan | Started: May 2026**

---

## What This LMS Project Already Teaches You

> Hands-on beats theory. You are building these — not just reading about them.

| # | Concept | Where in LMS |
|---|---------|-------------|
| 1 | **Microservices Architecture** | Every service independent — Live, Auth, Payment, Notification |
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
| 13 | **Caching Strategy** | Redis — sessions, room state, fast lookups |
| 14 | **Capacity Planning** | 540p × 500 users per 1Gbps — real math |
| 15 | **Centralized Logging** | ELK Stack — Log Collector service |

> This project covers ~70% of what FAANG system design interviews ask.

---

## What You Still Need to Cover

> These are NOT in the LMS project but are asked in Google / Amazon / Flipkart interviews.

| # | Topic | Why It's Asked |
|---|-------|---------------|
| 1 | **Consistent Hashing** | How distributed caches/DBs route requests — every senior interview |
| 2 | **CAP Theorem** | CP vs AP — why PostgreSQL vs Cassandra |
| 3 | **Database Sharding** | Split DB when one server can't handle data |
| 4 | **Database Replication** | Master-slave, read replicas — high availability |
| 5 | **Rate Limiting** | Token bucket / leaky bucket — API protection |
| 6 | **CDN** | How Netflix/YouTube serve video globally fast |
| 7 | **Bloom Filter** | Check if item exists without loading full DB |
| 8 | **Distributed Transactions (SAGA)** | Payment across microservices — handle failure |
| 9 | **Circuit Breaker** | Service A fails → don't cascade to Service B |
| 10 | **Service Discovery** | How microservices find each other — Eureka/Consul |
| 11 | **Idempotency** | Same request twice → same result, no duplicate payment |
| 12 | **Long Polling vs SSE vs WebSocket** | When to use which for real-time |
| 13 | **Message Queue vs Event Streaming** | RabbitMQ vs Kafka — difference and when |
| 14 | **Distributed Locking** | Two servers doing same thing — Redis SETNX |
| 15 | **Two-Phase Commit (2PC)** | Distributed DB consistency — why it's slow |

---

## HLD — High Level Design

> Big picture. No code. Boxes and arrows. Interviewer wants to see your thinking.

### HLD Framework (45 min)

| Step | Time | What to do |
|------|------|-----------|
| 1 — Clarify Requirements | 5 min | Functional (what it does) + Non-functional (scale, latency, availability) |
| 2 — Estimate Scale | 3 min | DAU, reads vs writes ratio, storage needed |
| 3 — API Design | 5 min | Endpoints, request/response shape |
| 4 — Database Design | 5 min | SQL vs NoSQL — why, schema sketch |
| 5 — High Level Diagram | 15 min | Client → Gateway → Services → DB, async flows, cache |
| 6 — Deep Dive Bottlenecks | 10 min | Where it fails at scale, how to fix |

### Classic HLD Problems to Practice

| Problem | Key Concepts |
|---------|-------------|
| Design YouTube | CDN, video encoding, storage, recommendation |
| Design WhatsApp | WebSocket, message queue, delivery receipts |
| Design Uber | Geo-indexing, real-time location, matching |
| Design Twitter Feed | Fan-out, cache, timeline generation |
| Design URL Shortener | Hashing, redirect, analytics |
| Design Rate Limiter | Token bucket, Redis, distributed counter |
| Design Notification System | Kafka, push/email/SMS, fan-out |
| **Design Live Class System** | SFU, WebRTC, token, recording — **you already built this** ✅ |

---

## LLD — Low Level Design

> Code level design. Class diagrams, design patterns, SOLID principles.

### LLD Framework

```
Step 1 — Identify Entities (nouns)
  → Parking Lot: ParkingLot, Floor, Slot, Vehicle, Ticket

Step 2 — Identify Behaviors (verbs)
  → park(), unpark(), findSlot(), generateTicket()

Step 3 — Apply Design Patterns
  → Factory, Strategy, Observer, Singleton

Step 4 — Write Class Diagram
  → Relationships: has-a, is-a, uses

Step 5 — Write key classes in code
  → Interfaces first, then implementations
```

### Design Patterns — Mapped to LMS

| Pattern | What it does | Real use in LMS |
|---------|-------------|----------------|
| **Singleton** | One instance shared everywhere | `RoomServiceClient` bean |
| **Factory** | Create objects without specifying exact class | Token types — student vs instructor |
| **Strategy** | Swap algorithms at runtime | Recording types — composite/track/RTMP |
| **Observer** | Notify multiple listeners on event | Webhook → attendance + recording + notification |
| **Builder** | Build complex objects step by step | Complex request objects |
| **Decorator** | Add behavior without changing class | Auth/logging wrappers on API calls |
| **Chain of Responsibility** | Pass request through chain of handlers | API Gateway — JWT → RBAC → Route |

### Classic LLD Problems to Practice

| Problem | Key Patterns |
|---------|-------------|
| Parking Lot | Factory, Singleton, Strategy |
| BookMyShow | Observer, Factory, Composite |
| Chess Game | Strategy, Command, State |
| ATM Machine | State, Command, Singleton |
| Elevator System | State, Strategy, Observer |
| **Live Session Service** | Singleton, Factory, Observer — **you already built this** ✅ |

---

## SOLID Principles

| Principle | One Line | Example in LMS |
|-----------|----------|---------------|
| **S** — Single Responsibility | One class, one job | `LiveKitTokenService` only generates tokens |
| **O** — Open/Closed | Open for extension, closed for modification | Add new recording type without changing existing code |
| **L** — Liskov Substitution | Subclass should replace parent safely | Student/Instructor both extend Participant |
| **I** — Interface Segregation | Don't force unused methods | Separate `TokenService` and `RoomService` interfaces |
| **D** — Dependency Inversion | Depend on abstraction, not implementation | `@Autowired RoomServiceClient` not `new RoomServiceClient()` |

---

## Interview Cheat Sheet

### "How would you scale this?"

1. Add caching (Redis) for read-heavy data
2. Add read replicas for DB
3. Shard DB if single table too large
4. Add CDN for static/video content
5. Use async (Kafka) for non-critical operations
6. Horizontal scaling behind load balancer

### "How do you handle failures?"

1. Retry with exponential backoff
2. Circuit breaker (Resilience4j)
3. Dead letter queue for failed Kafka messages
4. Idempotency keys for payments
5. Saga pattern for distributed transactions

### Numbers Every Engineer Should Know

| Thing | Approximate value |
|-------|------------------|
| Read from RAM | ~100 ns |
| Read from SSD | ~100 µs |
| Network round trip (same DC) | ~1 ms |
| Read from HDD | ~10 ms |
| 1 Gbps bandwidth | ~500 users at 540p video |
| Redis throughput | ~100k ops/sec |
| Kafka throughput | ~1M messages/sec |
| PostgreSQL (single node) | ~10k writes/sec |

---

## Study Order

| Phase | What | When |
|-------|------|------|
| **Phase 1** | Finish LMS Project — 15 concepts hands-on | Now |
| **Phase 2** | Fill gaps — CAP, Sharding, SAGA, Circuit Breaker | After LMS |
| **Phase 3** | HLD practice — 8-10 classic problems, 45 min each | 1 month |
| **Phase 4** | LLD practice — 5-6 classic problems, write actual code | 1 month |
| **Phase 5** | Mock interviews — Pramp / Interviewing.io / peer | Final prep |

**Best book:** *Designing Data-Intensive Applications* (DDIA) by Martin Kleppmann — covers Phase 2 completely.

---

*Built on actual LMS project + FAANG interview patterns | Updated: May 2026*
