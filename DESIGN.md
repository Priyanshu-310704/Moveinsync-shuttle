# Case Study Design - Interview Notes

## 1. Problem in one sentence
Book a physical shuttle seat for a stop interval, while allowing the same seat to be reused for non-overlapping intervals.

## 2. Core interval rule
Represent every booking as `[fromOrder, toOrder)`.

Overlap iff:
`newFrom < existingTo && newTo > existingFrom`

No overlap iff:
`newTo <= existingFrom || newFrom >= existingTo`

The half-open interval is important: A->B and B->D touch at B but do not share road time, so both are valid.

## 3. Data model
- Route(id, name)
- Stop(id, route_id, stop_order, name, arrival_time)
- Trip(id, route_id, trip_date, seat_count)
- Booking(id, trip_id, user_id, seat_number, from_order, to_order, created_at)
- WaitlistEntry(id, trip_id, user_id, from_order, to_order, created_at)
- AppUser(id, email, password_hash, role)

## 4. Booking algorithm
1. Validate `from < to` and that both stops belong to the trip route.
2. Start a database transaction.
3. Lock the trip row (`PESSIMISTIC_WRITE`) so two last-seat requests for the same trip cannot both make the same decision.
4. Query only bookings whose intervals overlap the requested segment.
5. Put their seat numbers in a Set.
6. Pick the first seat from `1..capacity` not in the Set.
7. Insert the booking and commit.
8. If none exists, return 409 or offer waitlist.

## 5. Why this handles the last-seat race
The critical availability check and booking insert happen inside one transaction while the trip row is locked. Request A gets the lock, books the seat, commits; request B then sees the committed booking and must choose another seat or fail. They cannot both successfully claim the same capacity under this lock strategy.

## 6. Complexity
Let S = number of seats and K = number of overlapping bookings returned by the database.
- Conflict lookup: database-index assisted rather than scanning all bookings in application memory.
- Seat selection: O(S) with a boolean/set representation; with a bitset it can be close to O(S/word_size).
- Memory for one request: O(K).

A future optimization for very large trips is a per-seat sorted interval index / segment tree, but PostgreSQL indexed conflict queries are simpler and easier to keep correct for this assignment.

## 7. Waitlist
Use FIFO (`created_at ASC`) per trip. On cancellation or no-show, re-check waitlisted requests in arrival order and promote the first request whose segment has any available seat now. Promotion runs in the same transactional critical section as cancellation/no-show.

This is safer than blindly promoting the first row or checking only whether a request is contained in the canceled passenger's exact segment. The authoritative test is always the same overlap query used by booking.

## 8. Caching
Cache read-heavy availability responses by `(tripId, fromOrder, toOrder)` in Redis. Invalidate the relevant availability keys after booking/cancellation/promotion. Never use cache as the source of truth for booking correctness; PostgreSQL transaction/lock is authoritative.

## 9. Failure handling
- Invalid segment -> 400
- Trip/booking not found -> 404
- No seat -> 409 / waitlist
- Duplicate registration -> 409 in final implementation
- Unexpected exception -> generic 500 response, detailed logs server-side
- Database outage -> fail closed; do not claim a booking without durable DB commit
- Cancellation + promotion must be atomic

## 10. Monitoring
Actuator endpoints: health, metrics, prometheus.
Track: booking latency, booking conflict rate, DB errors, cache hit ratio, waitlist size, failed requests.

## 11. Trade-offs to explain
### Row lock vs distributed lock
Row lock is simple and correct because PostgreSQL is already the source of truth. Redis distributed locks add operational complexity and failure modes.

### Cache vs correctness
Cache improves read latency but must never decide whether a booking succeeds. Stale cache is acceptable for display; it is not acceptable for the final booking decision.

### First available seat vs balanced allocation
First available minimizes implementation complexity. A more advanced allocator could optimize seat reuse patterns or fairness, but is unnecessary for the core requirement.

### Segment tree vs SQL overlap query
A segment tree can make repeated interval queries very fast in memory, but requires synchronization and rebuilding. Indexed SQL keeps the authoritative state transactional and is easier to explain and maintain for this assignment.

## 12. Edge cases
- `A->B` + `B->D`: valid reuse.
- `A->C` + `B->D`: conflict.
- Same exact segment twice: conflict.
- Request `A->A`: invalid.
- Request beyond final stop: invalid.
- Two concurrent requests for last overlapping seat: only one succeeds.
- Cancel then waitlist promotion: must happen atomically.
- No-show: mark booking `NO_SHOW`; availability queries ignore it and promotion runs.
- Mid-route boarding: represented naturally by `fromOrder > 0`.
- Trip moved to another bus: separate `bus_id`/vehicle assignment entity should be added; bookings stay attached to the trip, not the physical bus.

## 13. OOP/SOLID explanation
- Entity classes model domain state.
- Service owns booking business rules.
- Repository owns persistence queries.
- Controller owns HTTP concerns.
- Constructor injection supports dependency inversion and unit testing.
- A future `SeatAllocationStrategy` interface could support FirstAvailable, BestFit, etc. without changing the booking service.

## 14. APIs for the final demo
POST `/api/auth/register`
Use HTTP Basic authentication with the registered email/password.
POST `/api/routes`
POST `/api/trips`
GET `/api/trips/{tripId}/availability?fromOrder=0&toOrder=2`
POST `/api/trips/{tripId}/bookings`
DELETE `/api/bookings/{bookingId}`
POST `/api/trips/{tripId}/waitlist`

## 15. Demo story
1. Create route A-B-C-D.
2. Create a trip with 2 seats.
3. Book A->B; seat 1.
4. Book B->D; seat 1 again. Demonstrates segment reuse.
5. Book A->C; seat 2.
6. Request B->D; seat 2 is blocked by A->C and seat 1 is blocked by B->D, so it fails / goes to waitlist.
7. Cancel B->D on seat 1.
8. FIFO waitlisted B->D request is promoted.
9. Run two concurrent booking requests and show only one gets the last overlapping seat.
