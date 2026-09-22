package com.moveinsync.shuttle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull Long userId,
        @Min(0) int fromOrder,
        @Min(1) int toOrder
) {
}
