# MoveInSync - Office Shuttle Segment-Based Seat Booking

Spring Boot backend for the Office Shuttle case study. The key idea is that a physical seat can be reused for non-overlapping parts of the same route.

## Core Rule

Every booking is represented as a half-open stop interval: `[fromOrder, toOrder)`.

Two bookings overlap when:

```text
newFrom < existingTo && newTo > existingFrom
```

So A->B `[0,1)` and B->D `[1,3)` can share seat 1, but A->C `[0,2)` and B->D `[1,3)` cannot.

## What Is Implemented

- Spring Security HTTP Basic auth with BCrypt password hashing.
- Route creation with ordered stops and timings.
- Trip creation with fixed seat capacity.
- Segment-aware availability API with Redis-backed caching.
- Transactional booking with pessimistic trip locking to prevent last-seat races.
- Cancellation and no-show handling.
- FIFO waitlist with smart promotion: after capacity changes, the earliest waitlisted request that actually fits is promoted.
- PostgreSQL persistence with indexed overlap queries.
- Actuator health/metrics/prometheus endpoints.
- Tests for interval reuse, overlap conflict, invalid segment, availability, and no-show promotion.

## Run Locally

Start infrastructure:

```bash
docker compose up -d
```

Run the app:

```bash
mvn spring-boot:run
```

Run tests:

```bash
mvn test
```

The app runs on `http://localhost:8080`.

## Demo Flow

1. Register a user.
2. Create route A -> B -> C -> D.
3. Create a trip for that route.
4. Book A->B and B->D; both should use the same seat.
5. Book A->C on another seat.
6. Try B->D when all overlapping capacity is full; it returns `409`.
7. Join waitlist.
8. Cancel or mark no-show; the waitlist promotion creates a confirmed booking.

Full curl examples are in `API_EXAMPLES.md`.

## Complexity

Let `S` be trip seat count and `K` be the number of overlapping bookings returned by the database for the requested segment.

- Overlap lookup: database-index assisted query on `trip_id,status,seat_number,from_order,to_order`.
- Seat choice: `O(S)` by scanning seats `1..capacity`.
- Request memory: `O(K)` for occupied seat numbers.

For very large fleets, a per-seat interval tree or segment tree can speed repeated availability checks, but the database query is easier to keep transactional and correct for this assignment.

## Failure Handling

- Invalid segment: `400 Bad Request`
- Missing trip/booking/route: `404 Not Found`
- No seat available: `409 Conflict`
- Duplicate registration: `409 Conflict`
- Redis failure: logged and ignored; PostgreSQL remains the source of truth.
- Database failure: transaction rolls back, so a successful response is never returned before durable persistence.

## Interview Notes

The hardest part is not CRUD. It is the combination of interval-based seat reuse, concurrency, and fair promotion after cancellation/no-show. The booking check and insert happen in one transaction while locking the trip row, so two concurrent users cannot both claim the last overlapping seat.
