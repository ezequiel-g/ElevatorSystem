package com.challenge.kindalab.elevator_system.components;

import com.challenge.kindalab.elevator_system.config.ElevatorConfig;
import com.challenge.kindalab.elevator_system.domain.Elevator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ElevatorFactory {

    public List<Elevator> build(List<ElevatorConfig> elevatorConfigs) {
        List<Elevator> elevators = new ArrayList<>();

        for (int i = 0; i < elevatorConfigs.size(); i++) {
            ElevatorConfig elevatorConfig = elevatorConfigs.get(i);
            Elevator elevator = Elevator.builder()
                    .id(i)
                    .type(elevatorConfig.getType())
                    .weightLimit(elevatorConfig.getWeightLimit())
                    .cabin(Elevator.Cabin.builder().floorNumber(0).weight(0).build())
                    .build();
            elevators.add(elevator);
        }

        return elevators;
    }

}
