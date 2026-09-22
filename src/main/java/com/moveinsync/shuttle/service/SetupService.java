package com.moveinsync.shuttle.service;

import com.moveinsync.shuttle.dto.CreateRouteRequest;
import com.moveinsync.shuttle.dto.CreateTripRequest;
import com.moveinsync.shuttle.dto.RouteResponse;
import com.moveinsync.shuttle.dto.TripResponse;
import com.moveinsync.shuttle.entity.Route;
import com.moveinsync.shuttle.entity.Stop;
import com.moveinsync.shuttle.entity.Trip;
import com.moveinsync.shuttle.exception.ApiException;
import com.moveinsync.shuttle.repository.RouteRepository;
import com.moveinsync.shuttle.repository.TripRepository;
import java.util.Comparator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetupService {
    private final RouteRepository routes;
    private final TripRepository trips;

    public SetupService(RouteRepository routes, TripRepository trips) {
        this.routes = routes;
        this.trips = trips;
    }

    @Transactional
    public RouteResponse createRoute(CreateRouteRequest request) {
        if (request.stops().size() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A route needs at least two stops");
        }

        Route route = new Route();
        route.setName(request.name());

        for (int i = 0; i < request.stops().size(); i++) {
            CreateRouteRequest.StopRequest stopRequest = request.stops().get(i);
            Stop stop = new Stop();
            stop.setStopOrder(i);
            stop.setName(stopRequest.name());
            stop.setArrivalTime(stopRequest.arrivalTime());
            route.addStop(stop);
        }

        return toResponse(routes.save(route));
    }

    @Transactional
    public TripResponse createTrip(CreateTripRequest request) {
        Route route = routes.findById(request.routeId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Route not found"));

        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setTripDate(request.tripDate());
        trip.setSeatCount(request.seatCount());

        Trip saved = trips.save(trip);
        return new TripResponse(saved.getId(), route.getId(), saved.getTripDate(), saved.getSeatCount());
    }

    private RouteResponse toResponse(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getName(),
                route.getStops().stream()
                        .sorted(Comparator.comparingInt(Stop::getStopOrder))
                        .map(stop -> new RouteResponse.StopResponse(
                                stop.getStopOrder(),
                                stop.getName(),
                                stop.getArrivalTime()
                        ))
                        .toList()
        );
    }
}
