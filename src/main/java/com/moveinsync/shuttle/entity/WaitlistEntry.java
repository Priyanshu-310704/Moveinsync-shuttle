package com.moveinsync.shuttle.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "waitlist_entries",
        indexes = @Index(name = "idx_waitlist_trip_segment_time", columnList = "trip_id,from_order,to_order,created_at")
)
public class WaitlistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Trip trip;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "from_order", nullable = false)
    private int fromOrder;

    @Column(name = "to_order", nullable = false)
    private int toOrder;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getFromOrder() {
        return fromOrder;
    }

    public void setFromOrder(int fromOrder) {
        this.fromOrder = fromOrder;
    }

    public int getToOrder() {
        return toOrder;
    }

    public void setToOrder(int toOrder) {
        this.toOrder = toOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
