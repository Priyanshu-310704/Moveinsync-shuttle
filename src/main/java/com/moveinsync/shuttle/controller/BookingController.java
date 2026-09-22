package com.moveinsync.shuttle.controller;

import com.moveinsync.shuttle.dto.AvailabilityResponse;
import com.moveinsync.shuttle.dto.BookingRequest;
import com.moveinsync.shuttle.dto.BookingResponse;
import com.moveinsync.shuttle.dto.WaitlistResponse;
import com.moveinsync.shuttle.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @GetMapping("/trips/{tripId}/availability")
    public AvailabilityResponse availability(
            @PathVariable Long tripId,
            @RequestParam @Min(0) int fromOrder,
            @RequestParam @Min(1) int toOrder
    ) {
        return service.availability(tripId, fromOrder, toOrder);
    }

    @PostMapping("/trips/{tripId}/bookings")
    public ResponseEntity<BookingResponse> book(
            @PathVariable Long tripId,
            @Valid @RequestBody BookingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.book(tripId, request));
    }

    @PostMapping("/trips/{tripId}/waitlist")
    public ResponseEntity<WaitlistResponse> waitlist(
            @PathVariable Long tripId,
            @Valid @RequestBody BookingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.joinWaitlist(tripId, request));
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> cancel(@PathVariable Long bookingId) {
        service.cancel(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bookings/{bookingId}/no-show")
    public ResponseEntity<Void> noShow(@PathVariable Long bookingId) {
        service.markNoShow(bookingId);
        return ResponseEntity.noContent().build();
    }
}
