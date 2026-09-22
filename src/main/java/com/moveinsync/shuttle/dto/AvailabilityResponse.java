package com.moveinsync.shuttle.dto;

import java.io.Serializable;
import java.util.List;

public record AvailabilityResponse(
        Long tripId,
        int fromOrder,
        int toOrder,
        int totalSeats,
        int availableSeatCount,
        List<Integer> availableSeats
) implements Serializable {
}
