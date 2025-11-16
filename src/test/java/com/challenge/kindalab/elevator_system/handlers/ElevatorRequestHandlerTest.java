package com.challenge.kindalab.elevator_system.handlers;

import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assumptions.assumeThat;

class ElevatorRequestHandlerTest {

    private static final int WEIGHT_LIMIT = 1000;

    private final ElevatorRequestHandler elevatorRequestHandler = new ElevatorRequestHandler();
    private final Elevator.ElevatorBuilder elevatorBuilder = Elevator.builder()
            .weightLimit(WEIGHT_LIMIT)
            .cabin(Elevator.Cabin.builder().floorNumber(0).weight(0).build());

    @Test
    void load_anEmptyElevator_shouldAddWeightCorrectly() {
        Elevator elevator = elevatorBuilder.build();
        int weight = 0;

        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void load_tooMuchWeight_shouldEnableAlarmStopEngineAndWeightNoChange() {
        Elevator elevator = elevatorBuilder.build();
        int weight = WEIGHT_LIMIT + 100;

        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(0);
        assertThat(elevator.isAlarm()).isEqualTo(true);
        assertThat(elevator.isStopEngine()).isEqualTo(true);
    }

    @Test
    void load_emptyElevatorWithNegativeWeight_shouldKeepValues() {
        Elevator elevator = elevatorBuilder.build();
        int weight = -1;

        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(0);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void load_anEmptyElevatorWithTwoWeights_shouldAddWeightCorrectly() {
        Elevator elevator = elevatorBuilder.build();
        int weight = 70;

        elevatorRequestHandler.load(elevator, weight);
        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight * 2);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void load_tooMuchWeightAndThenReduceLoad_shouldEnableAlarmStopEngineAndWeightNoChange() {
        Elevator elevator = elevatorBuilder.build();
        int weight = 100;

        elevatorRequestHandler.load(elevator, weight);
        elevatorRequestHandler.load(elevator, weight);
        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight * 3);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);

        elevatorRequestHandler.load(elevator, WEIGHT_LIMIT);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight * 3);
        assertThat(elevator.isAlarm()).isEqualTo(true);
        assertThat(elevator.isStopEngine()).isEqualTo(true);

        elevatorRequestHandler.load(elevator, -weight);
        assertThat(elevator.getCabin().getWeight()).isEqualTo(weight * 2);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void call_emptyRequests_shouldAddNewRequest() {
        Elevator elevator = elevatorBuilder.build();
        assumeThat(elevator.getElevatorRequests()).isEmpty();
        Floor floor = new Floor(3, null);

        elevatorRequestHandler.call(elevator, floor);
        assertThat(elevator.getElevatorRequests())
                .hasSize(1)
                .contains(entry(floor.getFloorNumber(), new ElevatorRequestHandler.FloorCallRequest(floor)));
    }

    @Test
    void call_withPreviousRequests_shouldHaveBothRequests() {
        Elevator elevator = elevatorBuilder.build();
        Floor floor1 = new Floor(3, null);
        elevator.getElevatorRequests().putIfAbsent(floor1.getFloorNumber(), new ElevatorRequestHandler.FloorCallRequest(floor1));
        assumeThat(elevator.getElevatorRequests()).isNotEmpty();
        Floor floor2 = new Floor(5, null);

        elevatorRequestHandler.call(elevator, floor2);
        assertThat(elevator.getElevatorRequests())
                .hasSize(2)
                .contains(entry(floor1.getFloorNumber(), new ElevatorRequestHandler.FloorCallRequest(floor1)))
                .contains(entry(floor2.getFloorNumber(), new ElevatorRequestHandler.FloorCallRequest(floor2)));
    }

    @Test
    void move_withStoppedCabinOnFloorGroundToThirdFloor_shouldMoveToThirdFloor() {
        Elevator elevator = elevatorBuilder.build();
        Floor floor = new Floor(3, null);
        ElevatorRequestHandler.FloorCallRequest request = new ElevatorRequestHandler.FloorCallRequest(floor);
        elevator.getElevatorRequests().putIfAbsent(request.getFloor().getFloorNumber(), request);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(0);
        assumeThat(elevator.getElevatorRequests()).hasSize(1);

        elevatorRequestHandler.move(elevator);
        assertThat(elevator.getCabin().getFloorNumber()).isEqualTo(floor.getFloorNumber());
    }

    @Test
    void move_withStoppedCabinOnFloorGroundToThirdFloorAndSeven_shouldMoveToSevenFloor() {
        Elevator elevator = elevatorBuilder.build();
        ElevatorRequestHandler.FloorCallRequest request1 = new ElevatorRequestHandler.FloorCallRequest(new Floor(3, null));
        elevator.getElevatorRequests().putIfAbsent(request1.getFloor().getFloorNumber(), request1);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(0);
        assumeThat(elevator.getElevatorRequests()).hasSize(1);

        ElevatorRequestHandler.FloorCallRequest request2 = new ElevatorRequestHandler.FloorCallRequest(new Floor(7, null));
        elevator.getElevatorRequests().putIfAbsent(request2.getFloor().getFloorNumber(), request2);
        assumeThat(elevator.getElevatorRequests()).hasSize(2);

        elevatorRequestHandler.move(elevator);
        assertThat(elevator.getCabin().getFloorNumber()).isEqualTo(request2.getFloor().getFloorNumber());
        assertThat(elevator.getElevatorRequests()).isEmpty();
    }

    @Test
    void move2_withStoppedCabinOnFifthToTenFloorAndSeven_shouldMoveToTenFloor() {
        Elevator elevator = elevatorBuilder.build();
        elevator.getCabin().setFloorNumber(5);
        ElevatorRequestHandler.FloorCallRequest request1 = new ElevatorRequestHandler.FloorCallRequest(new Floor(10, null));
        elevator.getElevatorRequests().putIfAbsent(request1.getFloor().getFloorNumber(), request1);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(5);
        assumeThat(elevator.getElevatorRequests()).hasSize(1);

        ElevatorRequestHandler.FloorCallRequest request2 = new ElevatorRequestHandler.FloorCallRequest(new Floor(3, null));
        elevator.getElevatorRequests().putIfAbsent(request2.getFloor().getFloorNumber(), request2);
        assumeThat(elevator.getElevatorRequests()).hasSize(2);

        elevatorRequestHandler.move(elevator);
        assertThat(elevator.getCabin().getFloorNumber()).isEqualTo(request1.getFloor().getFloorNumber());
        assertThat(elevator.getElevatorRequests()).hasSize(1)
                .containsKey(request2.getFloor().getFloorNumber());
    }

    @Test
    void move_withStoppedCabinMovingDown_shouldMoveToFirstFloor() {
        Elevator elevator = elevatorBuilder.build();
        elevator.getCabin().setFloorNumber(5);
        ElevatorRequestHandler.FloorCallRequest request1 = new ElevatorRequestHandler.FloorCallRequest(new Floor(3, null));
        elevator.getElevatorRequests().putIfAbsent(request1.getFloor().getFloorNumber(), request1);
        assumeThat(elevator.getCabin().getFloorNumber()).isEqualTo(5);
        assumeThat(elevator.getElevatorRequests()).hasSize(1);

        ElevatorRequestHandler.FloorCallRequest request2 = new ElevatorRequestHandler.FloorCallRequest(new Floor(1, null));
        elevator.getElevatorRequests().putIfAbsent(request2.getFloor().getFloorNumber(), request2);
        assumeThat(elevator.getElevatorRequests()).hasSize(2);

        elevatorRequestHandler.move(elevator);
        assertThat(elevator.getCabin().getFloorNumber()).isEqualTo(request2.getFloor().getFloorNumber());
        assertThat(elevator.getElevatorRequests()).isEmpty();
    }


}