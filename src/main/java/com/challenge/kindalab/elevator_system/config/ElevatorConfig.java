package com.challenge.kindalab.elevator_system.config;

import com.challenge.kindalab.elevator_system.domain.Elevator;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ElevatorConfig {
    Elevator.ElevatorType type;
    int weightLimit;

}
