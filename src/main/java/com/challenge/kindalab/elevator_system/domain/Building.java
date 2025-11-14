package com.challenge.kindalab.elevator_system.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.NavigableMap;

@Builder
@Data
public class Building {

    private final NavigableMap<Integer, Floor> floors;
    private final List<Elevator> elevators;

}
