package com.moveinsync.shuttle.dto;

import java.time.LocalTime;
import java.util.List;

public record RouteResponse(
        Long routeId,
        String name,
        List<StopResponse> stops
) {
    public record StopResponse(
            int stopOrder,
            String name,
            LocalTime arrivalTime
    ) {
    }
}
