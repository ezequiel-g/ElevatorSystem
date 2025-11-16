package com.challenge.kindalab.elevator_system.config;

import com.challenge.kindalab.elevator_system.domain.Elevator;
import lombok.Value;

@Value
public class ElevatorConfig {

    Elevator.Type type;
    int weightLimit;

}
