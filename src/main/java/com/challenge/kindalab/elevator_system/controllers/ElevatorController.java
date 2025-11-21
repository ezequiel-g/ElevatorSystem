package com.challenge.kindalab.elevator_system.controllers;

import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import com.challenge.kindalab.elevator_system.handlers.ElevatorRequestHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RequestMapping("elevators")
@RestController
public class ElevatorController {

    private final ElevatorRequestHandler elevatorRequestHandler;
    private final Building building;

    @GetMapping("")
    ResponseEntity<List<Elevator>> getElevators() {
        return ResponseEntity.ok(building.getElevators());
    }

    @PutMapping("{id}/call/{floorNumber}")
    ResponseEntity<Elevator> call(@PathVariable int id, @PathVariable int floorNumber) {
        Elevator elevator = getElevator(id);
        Floor floor = getFloor(floorNumber);

        elevatorRequestHandler.call(elevator, floor);

        return ResponseEntity.ok(elevator);
    }

    private Floor getFloor(int floorNumber) {
        Floor floor = building.getFloors().get(floorNumber);
        if (Objects.isNull(floor)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Floor not found, please check provided floorNumber: " + floorNumber);
        }
        return floor;
    }

    private Elevator getElevator(int id) {
        try {
            return building.getElevators().get(id);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Elevator not found, please check provided id: " + id);
        }
    }

    @PutMapping("{id}/load/{floorNumber}")
    ResponseEntity<Elevator> load(@PathVariable int id, @PathVariable int floorNumber, @RequestParam int weight) {
        Elevator elevator = getElevator(id);
        getFloor(floorNumber);

        if (elevator.getCabin().getFloorNumber() != floorNumber) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Elevator Cabin isn't on this floor yet, please use floorNumber: " + elevator.getCabin().getFloorNumber());
        }

        elevatorRequestHandler.load(elevator, weight);

        return ResponseEntity.ok(elevator);
    }

    @PutMapping("{id}/move")
    ResponseEntity<Elevator> move(@PathVariable int id) {
        Elevator elevator = getElevator(id);

        if (elevator.getElevatorRequests().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "There are not pending requests for Elevator, you can add request by using elevator call or elevator destination APIs");
        }

        elevatorRequestHandler.move(elevator);

        return ResponseEntity.ok(elevator);
    }

    @PutMapping("{id}/destination/{floorNumber}")
    ResponseEntity<Elevator> destination(@PathVariable int id, @PathVariable int floorNumber, @RequestParam Optional<UUID> keycard) {
        Elevator elevator = getElevator(id);
        Floor floor = getFloor(floorNumber);

        if (Elevator.Type.FREIGHT.equals(elevator.getType())) {
            elevatorRequestHandler.call(elevator, floor);
        } else {
            if (Objects.isNull(floor.getKeycard())) {
                elevatorRequestHandler.call(elevator, floor);
            } else if (keycard.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Keycard is required to reach the floor: " + floor.getFloorNumber());
            } else if (UUID.fromString(floor.getKeycard()).equals(keycard.get())) {
                elevatorRequestHandler.call(elevator, floor);
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Provided keycard does match, please check keycard for floorNumber: " + floor.getFloorNumber());
            }
        }

        return ResponseEntity.ok(elevator);
    }

}
