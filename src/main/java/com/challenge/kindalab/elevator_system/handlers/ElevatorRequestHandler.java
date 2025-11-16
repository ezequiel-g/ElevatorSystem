package com.challenge.kindalab.elevator_system.handlers;

import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import lombok.Builder;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.SortedMap;

@Component
public class ElevatorRequestHandler {

    public void load(Elevator elevator, int weight) {
        int newWeight = elevator.getCabin().getWeight() + weight;
        if (newWeight <= elevator.getWeightLimit()) {
            elevator.getCabin().setWeight(Math.max(0, newWeight));
            elevator.setStopEngine(false);
            elevator.setAlarm(false);
        } else {
            elevator.setStopEngine(true);
            elevator.setAlarm(true);
        }
    }

    public void call(Elevator elevator, Floor floor) {
        if (elevator.getCabin().getFloorNumber() == floor.getFloorNumber()) {
            throw new RuntimeException("do nothing, Elevator Cabin is on the same floor.");
        }
        FloorCallRequest floorCall = new FloorCallRequest(floor);
        elevator.getElevatorRequests().putIfAbsent(floor.getFloorNumber(), floorCall);
    }

    public void move(Elevator elevator) {
        if (elevator.getElevatorRequests().isEmpty()) {
            throw new RuntimeException(("do nothing, there are no pending requests"));
        }
        if (Elevator.Status.STOPPED.equals(elevator.getStatus())) {
            // TODO: select the direction base on the closest floor requests
            SortedMap<Integer, FloorCallRequest> tailMap = elevator.getElevatorRequests().tailMap(elevator.getCabin().getFloorNumber());
            if (!tailMap.isEmpty()) {
                //tailMap.keySet().stream().toList().stream().sorted().forEach(
                tailMap.keySet().stream().sorted().toList().forEach(k -> {
                    elevator.getCabin().setFloorNumber(k);
                    elevator.getElevatorRequests().remove(k);
                });
            } else {
                SortedMap<Integer, FloorCallRequest> headMap = elevator.getElevatorRequests().headMap(elevator.getCabin().getFloorNumber());
                headMap.keySet().stream().sorted(Collections.reverseOrder()).forEach(k -> {
                    elevator.getCabin().setFloorNumber(k);
                    elevator.getElevatorRequests().remove(k);
                });
                System.out.println(tailMap);
                // moving up
            }
        }
    }

    @Builder
    @Value
    public static class FloorCallRequest {
        Floor floor;
    }

}