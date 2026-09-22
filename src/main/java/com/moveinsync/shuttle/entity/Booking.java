package com.moveinsync.shuttle.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "bookings",
        indexes = @Index(
                name = "idx_booking_trip_status_seat_segment",
                columnList = "trip_id,status,seat_number,from_order,to_order"
        )
)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Trip trip;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int seatNumber;

    @Column(name = "from_order", nullable = false)
    private int fromOrder;

    @Column(name = "to_order", nullable = false)
    private int toOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

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

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
