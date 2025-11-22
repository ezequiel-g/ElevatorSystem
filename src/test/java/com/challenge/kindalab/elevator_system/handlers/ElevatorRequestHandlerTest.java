package com.challenge.kindalab.elevator_system.handlers;

import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.assumeThat;

class ElevatorRequestHandlerTest {

    private static final int WEIGHT_LIMIT = 1000;

    private final Elevator.ElevatorBuilder elevatorBuilder = Elevator.builder()
            .weightLimit(WEIGHT_LIMIT)
            .cabin(Elevator.Cabin.builder().floorNumber(0).weight(0).build());
    private final ElevatorRequestHandler elevatorRequestHandler = new ElevatorRequestHandler();

    @Test
    void load_whenCabinIsEmpty_shouldAddWeightCorrectly() {
        Elevator elevator = elevatorBuilder.build();
        int weight = 0;

        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void load_whenWeightLoadedExceedLimit_shouldEnableAlarmStopEngine() {
        Elevator elevator = elevatorBuilder.build();
        int weight = WEIGHT_LIMIT + 100;

        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(WEIGHT_LIMIT + 100);
        assertThat(elevator.isAlarm()).isEqualTo(true);
        assertThat(elevator.isStopEngine()).isEqualTo(true);
    }

    @Test
    void load_whenNegativeWeightIsLoadedOnEmptyCabin_shouldSetWeight0() {
        Elevator elevator = elevatorBuilder.build();
        int weight = -1;

        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(0);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void load_whenEmptyIsCabinLoadedTwice_shouldAddWeightCorrectly() {
        Elevator elevator = elevatorBuilder.build();
        int weight = 70;

        elevatorRequestHandler.load(elevator, weight);
        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight * 2);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void load_whenAddingAndRemovingWeight_shouldChangeStatusCorrectly() {
        Elevator elevator = elevatorBuilder.build();
        int weight = 100;

        elevatorRequestHandler.load(elevator, weight);
        elevatorRequestHandler.load(elevator, weight);
        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight * 3);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);

        elevatorRequestHandler.load(elevator, WEIGHT_LIMIT);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight * 3 + WEIGHT_LIMIT);
        assertThat(elevator.isAlarm()).isEqualTo(true);
        assertThat(elevator.isStopEngine()).isEqualTo(true);

        elevatorRequestHandler.load(elevator, -weight * 4);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(WEIGHT_LIMIT - weight);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void call_whenCabinIsOnTheSameFloor_shouldThrownException() {
        Elevator elevator = elevatorBuilder.build();
        Floor floor = new Floor(0, null);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(floor.getFloorNumber());

        assertThatThrownBy(() -> elevatorRequestHandler.call(elevator, floor)).isExactlyInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"Discard call. Elevator Cabin is on the same floor: %s\"", floor.getFloorNumber());
    }

    @Test
    void call_whenRequestsIsEmpty_shouldAddNewRequest() {
        Elevator elevator = elevatorBuilder.build();
        assumeThat(elevator.getElevatorRequests()).isEmpty();
        Floor floor = new Floor(3, null);

        elevatorRequestHandler.call(elevator, floor);
        assertThat(elevator.getElevatorRequests())
                .hasSize(1)
                .contains(entry(floor.getFloorNumber(), new ElevatorRequestHandler.FloorRequest(floor)));
    }

    @Test
    void call_whenThereArePreviousRequests_shouldHaveBothRequests() {
        Elevator elevator = elevatorBuilder.build();
        Floor floor1 = new Floor(3, null);
        elevator.getElevatorRequests().putIfAbsent(floor1.getFloorNumber(), new ElevatorRequestHandler.FloorRequest(floor1));
        assumeThat(elevator.getElevatorRequests()).isNotEmpty();
        Floor floor2 = new Floor(5, null);

        elevatorRequestHandler.call(elevator, floor2);
        assertThat(elevator.getElevatorRequests())
                .hasSize(2)
                .contains(entry(floor1.getFloorNumber(), new ElevatorRequestHandler.FloorRequest(floor1)))
                .contains(entry(floor2.getFloorNumber(), new ElevatorRequestHandler.FloorRequest(floor2)));
    }

    @Test
    void move_whenRequestIsEmpty_shouldThrownException() {
        Elevator elevator = elevatorBuilder.build();
        assumeThat(elevator.getElevatorRequests()).isEmpty();

        assertThatThrownBy(() -> elevatorRequestHandler.move(elevator)).isExactlyInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"There are no pending requests for elevator id: %s\"", elevator.getId());
    }

    @Test
    void move_whenIsEngineStopped_shouldThrownException() {
        Elevator elevator = elevatorBuilder.stopEngine(true).build();
        Floor floor1 = new Floor(1, null);
        elevator.getElevatorRequests().putIfAbsent(floor1.getFloorNumber(), new ElevatorRequestHandler.FloorRequest(floor1));
        assumeThat(elevator.getElevatorRequests()).isNotEmpty();

        assertThatThrownBy(() -> elevatorRequestHandler.move(elevator)).isExactlyInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"Elevator Cabin weight limit exceed. Please, remove some weight\"", elevator.getId());
    }

    @Test
    void move_whenIsMoving_shouldThrownException() {
        Elevator elevator = elevatorBuilder.status(Elevator.Status.MOVING_UP).build();
        Floor floor1 = new Floor(1, null);
        elevator.getElevatorRequests().putIfAbsent(floor1.getFloorNumber(), new ElevatorRequestHandler.FloorRequest(floor1));
        assumeThat(elevator.getElevatorRequests()).isNotEmpty();

        assertThatThrownBy(() -> elevatorRequestHandler.move(elevator)).isExactlyInstanceOf(ResponseStatusException.class)
                .hasMessage("501 NOT_IMPLEMENTED \"Simulating handling request while elevator is moving not supported\"");
    }

    @Test
    void move_whenCabinIsStoppedOnFloorGroundToThirdFloor_shouldMoveToThirdFloor() {
        Elevator elevator = elevatorBuilder.build();
        Floor floor = new Floor(3, null);
        ElevatorRequestHandler.FloorRequest request = new ElevatorRequestHandler.FloorRequest(floor);
        elevator.getElevatorRequests().putIfAbsent(request.getFloor().getFloorNumber(), request);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(0);
        assumeThat(elevator.getElevatorRequests()).hasSize(1);

        elevatorRequestHandler.move(elevator);
        assertThat(elevator.getCabin().getFloorNumber()).isEqualTo(floor.getFloorNumber());
    }

    @Test
    void move_whenStoppedCabinOnFloorGroundToThirdFloorAndSeven_shouldMoveToSevenFloor() {
        Elevator elevator = elevatorBuilder.build();
        ElevatorRequestHandler.FloorRequest request1 = new ElevatorRequestHandler.FloorRequest(new Floor(3, null));
        elevator.getElevatorRequests().putIfAbsent(request1.getFloor().getFloorNumber(), request1);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(0);
        assumeThat(elevator.getElevatorRequests()).hasSize(1);

        ElevatorRequestHandler.FloorRequest request2 = new ElevatorRequestHandler.FloorRequest(new Floor(7, null));
        elevator.getElevatorRequests().putIfAbsent(request2.getFloor().getFloorNumber(), request2);
        assumeThat(elevator.getElevatorRequests()).hasSize(2);

        elevatorRequestHandler.move(elevator);
        assertThat(elevator.getCabin().getFloorNumber()).isEqualTo(request2.getFloor().getFloorNumber());
        assertThat(elevator.getElevatorRequests()).isEmpty();
    }

    @Test
    void move_whenStoppedCabinOnFifthToTenFloorAndSeven_shouldMoveToTenFloor() {
        Elevator elevator = elevatorBuilder.build();
        elevator.getCabin().setFloorNumber(5);
        ElevatorRequestHandler.FloorRequest request1 = new ElevatorRequestHandler.FloorRequest(new Floor(10, null));
        elevator.getElevatorRequests().putIfAbsent(request1.getFloor().getFloorNumber(), request1);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(5);
        assumeThat(elevator.getElevatorRequests()).hasSize(1);

        ElevatorRequestHandler.FloorRequest request2 = new ElevatorRequestHandler.FloorRequest(new Floor(3, null));
        elevator.getElevatorRequests().putIfAbsent(request2.getFloor().getFloorNumber(), request2);
        assumeThat(elevator.getElevatorRequests()).hasSize(2);

        elevatorRequestHandler.move(elevator);
        assertThat(elevator.getCabin().getFloorNumber()).isEqualTo(request1.getFloor().getFloorNumber());
        assertThat(elevator.getElevatorRequests()).hasSize(1)
                .containsKey(request2.getFloor().getFloorNumber());
    }

    @Test
    void move_whenStoppedCabinOnFifthFloorMovingDown_shouldMoveToFirstFloor() {
        Elevator elevator = elevatorBuilder.build();
        elevator.getCabin().setFloorNumber(5);
        ElevatorRequestHandler.FloorRequest request1 = new ElevatorRequestHandler.FloorRequest(new Floor(3, null));
        elevator.getElevatorRequests().putIfAbsent(request1.getFloor().getFloorNumber(), request1);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(5);
        assumeThat(elevator.getElevatorRequests()).hasSize(1);

        ElevatorRequestHandler.FloorRequest request2 = new ElevatorRequestHandler.FloorRequest(new Floor(1, null));
        elevator.getElevatorRequests().putIfAbsent(request2.getFloor().getFloorNumber(), request2);
        assumeThat(elevator.getElevatorRequests()).hasSize(2);

        elevatorRequestHandler.move(elevator);
        assertThat(elevator.getCabin().getFloorNumber()).isEqualTo(request2.getFloor().getFloorNumber());
        assertThat(elevator.getElevatorRequests()).isEmpty();
    }


}