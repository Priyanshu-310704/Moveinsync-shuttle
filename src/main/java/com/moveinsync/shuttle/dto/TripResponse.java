package com.moveinsync.shuttle.dto;

import java.time.LocalDate;

public record TripResponse(
        Long tripId,
        Long routeId,
        LocalDate tripDate,
        int seatCount
) {
}
