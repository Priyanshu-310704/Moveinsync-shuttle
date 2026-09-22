package com.moveinsync.shuttle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateTripRequest(
        @NotNull Long routeId,
        @NotNull LocalDate tripDate,
        @Min(1) int seatCount
) {
}
