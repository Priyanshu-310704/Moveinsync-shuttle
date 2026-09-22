package com.moveinsync.shuttle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moveinsync.shuttle.dto.AvailabilityResponse;
import com.moveinsync.shuttle.dto.BookingRequest;
import com.moveinsync.shuttle.dto.BookingResponse;
import com.moveinsync.shuttle.entity.Booking;
import com.moveinsync.shuttle.entity.BookingStatus;
import com.moveinsync.shuttle.entity.Route;
import com.moveinsync.shuttle.entity.Stop;
import com.moveinsync.shuttle.entity.Trip;
import com.moveinsync.shuttle.exception.ApiException;
import com.moveinsync.shuttle.repository.BookingRepository;
import com.moveinsync.shuttle.repository.RouteRepository;
import com.moveinsync.shuttle.repository.TripRepository;
import com.moveinsync.shuttle.repository.WaitlistRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceTest {
    @Autowired
    private BookingService service;

    @Autowired
    private RouteRepository routes;

    @Autowired
    private TripRepository trips;

    @Autowired
    private BookingRepository bookings;

    @Autowired
    private WaitlistRepository waitlist;

    private Long tripId;

    @BeforeEach
    void setUp() {
        waitlist.deleteAll();
        bookings.deleteAll();
        trips.deleteAll();
        routes.deleteAll();

        Route route = new Route();
        route.setName("Office Shuttle");
        route.addStop(stop(0, "A", "10:00"));
        route.addStop(stop(1, "B", "10:15"));
        route.addStop(stop(2, "C", "10:30"));
        route.addStop(stop(3, "D", "10:45"));
        Route savedRoute = routes.save(route);

        Trip trip = new Trip();
        trip.setRoute(savedRoute);
        trip.setTripDate(LocalDate.of(2026, 9, 22));
        trip.setSeatCount(2);
        tripId = trips.save(trip).getId();
    }

    @Test
    void touchingSegmentsReuseTheSameSeat() {
        BookingResponse first = service.book(tripId, new BookingRequest(1L, 0, 1));
        BookingResponse second = service.book(tripId, new BookingRequest(2L, 1, 3));

        assertThat(first.seatNumber()).isEqualTo(1);
        assertThat(second.seatNumber()).isEqualTo(1);
    }

    @Test
    void overlappingSegmentsNeedDifferentSeatsAndCanRunOut() {
        BookingResponse first = service.book(tripId, new BookingRequest(1L, 0, 2));
        BookingResponse second = service.book(tripId, new BookingRequest(2L, 1, 3));

        assertThat(first.seatNumber()).isEqualTo(1);
        assertThat(second.seatNumber()).isEqualTo(2);

        assertThatThrownBy(() -> service.book(tripId, new BookingRequest(3L, 1, 2)))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).status())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void availabilityReportsOnlySeatsWithoutOverlappingConfirmedBookings() {
        service.book(tripId, new BookingRequest(1L, 0, 1));
        service.book(tripId, new BookingRequest(2L, 0, 2));

        AvailabilityResponse availability = service.availability(tripId, 1, 3);

        assertThat(availability.availableSeatCount()).isEqualTo(1);
        assertThat(availability.availableSeats()).containsExactly(1);
    }

    @Test
    void invalidSegmentIsRejected() {
        assertThatThrownBy(() -> service.book(tripId, new BookingRequest(1L, 2, 2)))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).status())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void noShowFreesSeatAndPromotesFirstEligibleWaitlistEntry() {
        Long oneSeatTrip = createTripWithSeatCount(1);
        BookingResponse booking = service.book(oneSeatTrip, new BookingRequest(1L, 0, 3));
        service.joinWaitlist(oneSeatTrip, new BookingRequest(2L, 1, 3));

        service.markNoShow(booking.bookingId());

        assertThat(waitlist.findByTripIdOrderByCreatedAtAsc(oneSeatTrip)).isEmpty();
        assertThat(bookings.findAll())
                .filteredOn(bookingRecord -> bookingRecord.getStatus() == BookingStatus.CONFIRMED)
                .extracting(Booking::getUserId)
                .contains(2L);
    }

    private Long createTripWithSeatCount(int seatCount) {
        Trip trip = new Trip();
        trip.setRoute(routes.findAll().get(0));
        trip.setTripDate(LocalDate.of(2026, 9, 23));
        trip.setSeatCount(seatCount);
        return trips.save(trip).getId();
    }

    private Stop stop(int order, String name, String time) {
        Stop stop = new Stop();
        stop.setStopOrder(order);
        stop.setName(name);
        stop.setArrivalTime(LocalTime.parse(time));
        return stop;
    }
}
