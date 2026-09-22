package com.moveinsync.shuttle.dto;

public record BookingResponse(
        Long bookingId,
        Long tripId,
        Long userId,
        int seatNumber,
        int fromOrder,
        int toOrder,
        String status
) {
}
