package com.challenge.kindalab.elevator_system.handlers;

import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.ElevatorRequest;
import com.challenge.kindalab.elevator_system.domain.Floor;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;

@Component
public class ElevatorRequestHandler {

    public void load(Elevator elevator, int weight) {
        int newWeight = elevator.getElevatorCar().getCargoWeight() + weight;
        if (newWeight <= elevator.getWeightLimit()) {
            elevator.getElevatorCar().setCargoWeight(Math.max(0, newWeight));
            elevator.setStopEngine(false);
            elevator.setAlarm(false);
        } else {
            elevator.setStopEngine(true);
            elevator.setAlarm(true);
        }
    }

    public void call(Elevator elevator, Floor floor) {
        if (elevator.getElevatorCar().getFloorNumber() == floor.getFloorNumber()) {
            throw new RuntimeException("do nothing, Elevator Car is on the same floor.");
        }
        FloorCallRequest floorCall = new FloorCallRequest(floor);
        elevator.getElevatorRequests().putIfAbsent(floor.getFloorNumber(), floorCall);
    }

    @Value
    @AllArgsConstructor
    public static class FloorCallRequest implements ElevatorRequest {
        Floor floor;
    }

}