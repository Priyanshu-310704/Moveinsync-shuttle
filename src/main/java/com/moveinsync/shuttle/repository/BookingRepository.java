package com.moveinsync.shuttle.repository;

import com.moveinsync.shuttle.entity.Booking;
import com.moveinsync.shuttle.entity.BookingStatus;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            select b.seatNumber
            from Booking b
            where b.trip.id = :tripId
              and b.status = :status
              and b.fromOrder < :toOrder
              and b.toOrder > :fromOrder
            """)
    Set<Integer> findOccupiedSeats(
            @Param("tripId") Long tripId,
            @Param("fromOrder") int fromOrder,
            @Param("toOrder") int toOrder,
            @Param("status") BookingStatus status
    );

    @Query("""
            select b
            from Booking b
            where b.trip.id = :tripId
              and b.seatNumber = :seat
              and b.status = :status
              and b.fromOrder < :toOrder
              and b.toOrder > :fromOrder
            """)
    List<Booking> findOverlaps(
            @Param("tripId") Long tripId,
            @Param("seat") int seat,
            @Param("fromOrder") int fromOrder,
            @Param("toOrder") int toOrder,
            @Param("status") BookingStatus status
    );
}
