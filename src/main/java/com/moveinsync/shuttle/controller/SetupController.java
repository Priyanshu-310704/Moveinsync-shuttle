package com.moveinsync.shuttle.controller;

import com.moveinsync.shuttle.dto.CreateRouteRequest;
import com.moveinsync.shuttle.dto.CreateTripRequest;
import com.moveinsync.shuttle.dto.RouteResponse;
import com.moveinsync.shuttle.dto.TripResponse;
import com.moveinsync.shuttle.service.SetupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SetupController {
    private final SetupService service;

    public SetupController(SetupService service) {
        this.service = service;
    }

    @PostMapping("/routes")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRoute(request));
    }

    @PostMapping("/trips")
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody CreateTripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTrip(request));
    }
}
