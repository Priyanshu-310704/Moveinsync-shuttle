package com.moveinsync.shuttle.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "stops", uniqueConstraints = @UniqueConstraint(columnNames = {"route_id", "stop_order"}))
public class Stop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Route route;

    @Column(name = "stop_order", nullable = false)
    private int stopOrder;

    @Column(nullable = false)
    private String name;

    private LocalTime arrivalTime;

    public Long getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(int stopOrder) {
        this.stopOrder = stopOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}
