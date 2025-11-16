package com.challenge.kindalab.elevator_system.domain;

import com.challenge.kindalab.elevator_system.handlers.ElevatorRequestHandler;
import lombok.Builder;
import lombok.Data;

import java.util.NavigableMap;
import java.util.TreeMap;

@Builder
@Data
public class Elevator {

    int id;
    Type type;
    int weightLimit;
    Cabin cabin;
    @Builder.Default
    Status status = Status.STOPPED;
    @Builder.Default
    NavigableMap<Integer, ElevatorRequestHandler.FloorCallRequest> elevatorRequests = new TreeMap<>();
    boolean stopEngine;
    boolean alarm;

    public enum Type {
        PUBLIC, FREIGHT
    }

    public enum Status {
        STOPPED, MOVING_UP, MOVING_DOWN
    }

    @Builder
    @Data
    public static class Cabin {
        int floorNumber;
        int weight;
    }

}
