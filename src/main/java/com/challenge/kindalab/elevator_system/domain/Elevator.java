package com.challenge.kindalab.elevator_system.domain;

import lombok.Builder;
import lombok.Data;

import java.util.NavigableMap;
import java.util.TreeMap;

@Builder
@Data
public class Elevator {

    int id;
    ElevatorType type;
    int weightLimit;
    ElevatorCar elevatorCar;
    @Builder.Default
    NavigableMap<Integer, ElevatorRequest> elevatorRequests = new TreeMap<>();
    boolean stopEngine;
    boolean alarm;

    public enum ElevatorType {
        PUBLIC, FREIGHT
    }

    @Builder
    @Data
    public static class ElevatorCar {
        int floorNumber;
        int cargoWeight;
    }

}
