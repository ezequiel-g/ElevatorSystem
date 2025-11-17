package com.challenge.kindalab.elevator_system.controllers;

import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import com.challenge.kindalab.elevator_system.handlers.ElevatorRequestHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RequestMapping("elevators")
@RestController
public class ElevatorController {

    private ElevatorRequestHandler elevatorRequestHandler;
    private Building building;

    @GetMapping("")
    ResponseEntity<List<Elevator>> getElevators() {
        return ResponseEntity.ok(building.getElevators());
    }

    @PutMapping("{id}/call/{floorNumber}")
    ResponseEntity<Elevator> call(@PathVariable int id, @PathVariable int floorNumber) {
        Elevator elevator = building.getElevators().get(id);
        Objects.requireNonNull(elevator, "please check the provided elevator id: " + id);
        Floor floor = building.getFloors().get(floorNumber);
        Objects.requireNonNull(floor, "please check the provided floorNumber: " + floorNumber);

        elevatorRequestHandler.call(elevator, floor);

        return ResponseEntity.ok(elevator);
    }

    @PutMapping("{id}/load/{floorNumber}")
    ResponseEntity<Elevator> load(@PathVariable int id, @PathVariable int floorNumber, @RequestParam int weight) {
        Elevator elevator = building.getElevators().get(id);
        Objects.requireNonNull(elevator, "please check the provided elevator id: " + id);
        Floor floor = building.getFloors().get(floorNumber);
        Objects.requireNonNull(floor, "please check the provided floorNumber: " + floorNumber);

        if (elevator.getCabin().getFloorNumber() != floorNumber) {
            throw new RuntimeException("do nothing, Elevator Cabin isn't on this floor yet.");
        }

        elevatorRequestHandler.load(elevator, weight);

        return ResponseEntity.ok(elevator);
    }

    @PutMapping("{id}/move")
    ResponseEntity<Elevator> move(@PathVariable int id) {
        Elevator elevator = building.getElevators().get(id);
        Objects.requireNonNull(elevator, "please check the provided elevator id: " + id);
        if (elevator.getElevatorRequests().isEmpty()) {
            throw new RuntimeException("do nothing, there are not pending requests.");
        }

        elevatorRequestHandler.move(elevator);

        return ResponseEntity.ok(elevator);
    }

    @PutMapping("{id}/destination/{floorNumber}")
    ResponseEntity<Elevator> destination(@PathVariable int id, @PathVariable int floorNumber, @RequestParam Optional<UUID> keycard) {
        Elevator elevator = building.getElevators().get(id);
        Objects.requireNonNull(elevator, "please check the provided elevator id: " + id);
        Floor floor = building.getFloors().get(floorNumber);
        Objects.requireNonNull(floor, "please check the provided floorNumber: " + floorNumber);

        if (Elevator.Type.FREIGHT.equals(elevator.getType())) {
            elevatorRequestHandler.call(elevator, floor);
        } else {
            if (Objects.isNull(floor.getKeycard())) {
                elevatorRequestHandler.call(elevator, floor);
            } else if (keycard.isEmpty()) {
                throw new RuntimeException("keycard is required to reach the floor: " + floor.getFloorNumber());
            } else if (UUID.fromString(floor.getKeycard()).equals(keycard.get())) {
                elevatorRequestHandler.call(elevator, floor);
            } else {
                throw new RuntimeException("provided keycard does match, please check it: " + keycard.get());
            }
        }

        return ResponseEntity.ok(elevator);
    }

}
