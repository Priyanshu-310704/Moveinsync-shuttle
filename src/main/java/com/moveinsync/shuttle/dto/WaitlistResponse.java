package com.moveinsync.shuttle.dto;

public record WaitlistResponse(
        Long waitlistId,
        Long tripId,
        Long userId,
        int fromOrder,
        int toOrder,
        String status
) {
}
