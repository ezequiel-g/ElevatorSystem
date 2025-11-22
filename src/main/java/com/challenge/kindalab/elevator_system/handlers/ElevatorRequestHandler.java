package com.challenge.kindalab.elevator_system.handlers;

import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import lombok.Builder;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.SortedMap;

@Component
public class ElevatorRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(ElevatorRequestHandler.class);

    public void load(Elevator elevator, int weight) {
        int totalWeight = elevator.getCabin().getWeight() + weight;
        if (totalWeight <= elevator.getWeightLimit()) {
            log.info("### loading Elevator Cabin with weight: {}. Total weight is: {}", weight, totalWeight);
            elevator.getCabin().setWeight(Math.max(0, totalWeight));
            elevator.setStopEngine(false);
            elevator.setAlarm(false);
        } else {
            log.warn("### loading Elevator Cabin exceed weight limit {}. Reduce cabin weight: {} by loading negative weight", elevator.getWeightLimit(), totalWeight);
            elevator.getCabin().setWeight(totalWeight);
            elevator.setStopEngine(true);
            elevator.setAlarm(true);
            log.warn("### loading Elevator Cabin exceed weight limit {}. Stop Engine is: {} and Alarm is: {}", elevator.getWeightLimit(), elevator.isStopEngine(), elevator.isAlarm());
        }
    }

    public void call(Elevator elevator, Floor floor) {
        if (elevator.getCabin().getFloorNumber() == floor.getFloorNumber()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discard call. Elevator Cabin is on the same floor: " + floor.getFloorNumber());
        }
        FloorRequest request = new FloorRequest(floor);
        elevator.getElevatorRequests().putIfAbsent(floor.getFloorNumber(), request);
        log.info("### Calling Elevator pending requests are: {}", elevator.getElevatorRequests());
    }

    public void move(Elevator elevator) {
        if (elevator.getElevatorRequests().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are no pending requests for elevator id: " + elevator.getId());
        }

        if (elevator.isStopEngine()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elevator Cabin weight limit exceed. Please, remove some weight");
        }

        if (Elevator.Status.STOPPED.equals(elevator.getStatus())) {
            SortedMap<Integer, FloorRequest> tailMap = elevator.getElevatorRequests().tailMap(elevator.getCabin().getFloorNumber());
            if (!tailMap.isEmpty()) {
                elevator.setStatus(Elevator.Status.MOVING_UP);
                tailMap.keySet().stream().sorted().toList().forEach(k -> {
                    log.info("### Moving Elevator direction: {} from: {} to: {} floor", elevator.getStatus(), elevator.getCabin().getFloorNumber(), k);
                    elevator.getCabin().setFloorNumber(k);
                    elevator.getElevatorRequests().remove(k);
                });
            } else {
                elevator.setStatus(Elevator.Status.MOVING_DOWN);
                SortedMap<Integer, FloorRequest> headMap = elevator.getElevatorRequests().headMap(elevator.getCabin().getFloorNumber());
                headMap.keySet().stream().sorted(Collections.reverseOrder()).forEach(k -> {
                    log.info("### Moving Elevator direction: {} from: {} to: {} floor", elevator.getStatus(), elevator.getCabin().getFloorNumber(), k);
                    elevator.getCabin().setFloorNumber(k);
                    elevator.getElevatorRequests().remove(k);
                });
            }
            elevator.setStatus(Elevator.Status.STOPPED);
        } else {
            // TODO: figure out how to manage request while the Elevator Cabin is moving. So far, does not support 2 actions at the same time.
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Simulating handling request while elevator is moving not supported");
        }

    }

    @Builder
    @Value
    public static class FloorRequest {
        Floor floor;
    }

}