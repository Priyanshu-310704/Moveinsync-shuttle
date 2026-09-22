package com.moveinsync.shuttle.service;

import com.moveinsync.shuttle.dto.AvailabilityResponse;
import com.moveinsync.shuttle.dto.BookingRequest;
import com.moveinsync.shuttle.dto.BookingResponse;
import com.moveinsync.shuttle.dto.WaitlistResponse;
import com.moveinsync.shuttle.entity.Booking;
import com.moveinsync.shuttle.entity.BookingStatus;
import com.moveinsync.shuttle.entity.Trip;
import com.moveinsync.shuttle.entity.WaitlistEntry;
import com.moveinsync.shuttle.exception.ApiException;
import com.moveinsync.shuttle.repository.BookingRepository;
import com.moveinsync.shuttle.repository.TripRepository;
import com.moveinsync.shuttle.repository.WaitlistRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final TripRepository trips;
    private final BookingRepository bookings;
    private final WaitlistRepository waitlist;

    public BookingService(TripRepository trips, BookingRepository bookings, WaitlistRepository waitlist) {
        this.trips = trips;
        this.bookings = bookings;
        this.waitlist = waitlist;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "availability", key = "#p0 + ':' + #p1 + ':' + #p2")
    public AvailabilityResponse availability(Long tripId, int fromOrder, int toOrder) {
        Trip trip = trips.findByIdWithStops(tripId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));
        validateSegment(trip, fromOrder, toOrder);

        Set<Integer> occupiedSeats = occupiedSeats(tripId, fromOrder, toOrder);
        return new AvailabilityResponse(
                tripId,
                fromOrder,
                toOrder,
                trip.getSeatCount(),
                trip.getSeatCount() - occupiedSeats.size(),
                availableSeats(trip, occupiedSeats)
        );
    }

    @Transactional
    @CacheEvict(cacheNames = "availability", allEntries = true)
    public BookingResponse book(Long tripId, BookingRequest request) {
        Trip trip = lockTrip(tripId);
        validateSegment(trip, request.fromOrder(), request.toOrder());

        return allocateSeat(trip, request.userId(), request.fromOrder(), request.toOrder())
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "No seat available for this segment"));
    }

    @Transactional
    @CacheEvict(cacheNames = "availability", allEntries = true)
    public WaitlistResponse joinWaitlist(Long tripId, BookingRequest request) {
        Trip trip = lockTrip(tripId);
        validateSegment(trip, request.fromOrder(), request.toOrder());

        WaitlistEntry entry = new WaitlistEntry();
        entry.setTrip(trip);
        entry.setUserId(request.userId());
        entry.setFromOrder(request.fromOrder());
        entry.setToOrder(request.toOrder());

        WaitlistEntry saved = waitlist.save(entry);
        return new WaitlistResponse(
                saved.getId(),
                tripId,
                saved.getUserId(),
                saved.getFromOrder(),
                saved.getToOrder(),
                "WAITLISTED"
        );
    }

    @Transactional
    @CacheEvict(cacheNames = "availability", allEntries = true)
    public void cancel(Long bookingId) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        Long tripId = booking.getTrip().getId();
        lockTrip(tripId);

        bookings.delete(booking);
        bookings.flush();
        promoteFirstEligible(tripId);
    }

    @Transactional
    @CacheEvict(cacheNames = "availability", allEntries = true)
    public void markNoShow(Long bookingId) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        Long tripId = booking.getTrip().getId();
        lockTrip(tripId);

        if (booking.getStatus() == BookingStatus.NO_SHOW) {
            return;
        }

        booking.setStatus(BookingStatus.NO_SHOW);
        bookings.saveAndFlush(booking);
        promoteFirstEligible(tripId);
    }

    private Trip lockTrip(Long tripId) {
        return trips.findByIdForUpdate(tripId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));
    }

    private Optional<Booking> allocateSeat(Trip trip, Long userId, int fromOrder, int toOrder) {
        Set<Integer> occupiedSeats = occupiedSeats(trip.getId(), fromOrder, toOrder);
        for (int seat = 1; seat <= trip.getSeatCount(); seat++) {
            if (!occupiedSeats.contains(seat)) {
                Booking booking = new Booking();
                booking.setTrip(trip);
                booking.setUserId(userId);
                booking.setSeatNumber(seat);
                booking.setFromOrder(fromOrder);
                booking.setToOrder(toOrder);
                return Optional.of(bookings.save(booking));
            }
        }
        return Optional.empty();
    }

    private void promoteFirstEligible(Long tripId) {
        Trip trip = lockTrip(tripId);
        for (WaitlistEntry entry : waitlist.findByTripIdOrderByCreatedAtAsc(tripId)) {
            validateSegment(trip, entry.getFromOrder(), entry.getToOrder());
            Optional<Booking> promoted = allocateSeat(
                    trip,
                    entry.getUserId(),
                    entry.getFromOrder(),
                    entry.getToOrder()
            );
            if (promoted.isPresent()) {
                waitlist.delete(entry);
                return;
            }
        }
    }

    private Set<Integer> occupiedSeats(Long tripId, int fromOrder, int toOrder) {
        return bookings.findOccupiedSeats(tripId, fromOrder, toOrder, BookingStatus.CONFIRMED);
    }

    private List<Integer> availableSeats(Trip trip, Set<Integer> occupiedSeats) {
        List<Integer> result = new ArrayList<>();
        for (int seat = 1; seat <= trip.getSeatCount(); seat++) {
            if (!occupiedSeats.contains(seat)) {
                result.add(seat);
            }
        }
        return result;
    }

    private void validateSegment(Trip trip, int fromOrder, int toOrder) {
        int stopCount = trip.getRoute().getStops().size();
        if (fromOrder < 0 || toOrder > stopCount - 1 || fromOrder >= toOrder) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid stop segment");
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getTrip().getId(),
                booking.getUserId(),
                booking.getSeatNumber(),
                booking.getFromOrder(),
                booking.getToOrder(),
                booking.getStatus().name()
        );
    }
}
