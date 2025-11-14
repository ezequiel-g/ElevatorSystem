package com.challenge.kindalab.elevator_system.handlers;

import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ElevatorRequestHandlerTest {

    private static final int WEIGHT_LIMIT = 1000;

    private final ElevatorRequestHandler elevatorRequestHandler = new ElevatorRequestHandler();
    private final Elevator.ElevatorBuilder elevatorBuilder = Elevator.builder()
            .weightLimit(WEIGHT_LIMIT)
            .elevatorCar(Elevator.ElevatorCar.builder().floorNumber(0).cargoWeight(0).build());

    @Test
    void load_anEmptyElevator_shouldAddWeightCorrectly() {
        Elevator elevator = elevatorBuilder.build();
        int weight = 0;
        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getElevatorCar().getCargoWeight()).isEqualTo(weight);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void load_tooMuchWeight_shouldEnableAlarmStopEngineAndWeightNoChange() {
        Elevator elevator = elevatorBuilder.build();
        int weight = WEIGHT_LIMIT + 100;
        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getElevatorCar().getCargoWeight()).isEqualTo(0);
        assertThat(elevator.isAlarm()).isEqualTo(true);
        assertThat(elevator.isStopEngine()).isEqualTo(true);
    }

    @Test
    void load_emptyElevatorWithNegativeWeight_shouldKeepValues() {
        Elevator elevator = elevatorBuilder.build();
        int weight = -1;
        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getElevatorCar().getCargoWeight()).isEqualTo(0);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void load_anEmptyElevatorWithTwoWeights_shouldAddWeightCorrectly() {
        Elevator elevator = elevatorBuilder.build();
        int weight = 70;
        elevatorRequestHandler.load(elevator, weight);
        elevatorRequestHandler.load(elevator, weight);
        assertThat(elevator.getElevatorCar().getCargoWeight()).isEqualTo(weight * 2);
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
        assertThat(elevator.getElevatorCar().getCargoWeight()).isEqualTo(weight * 3);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);

        elevatorRequestHandler.load(elevator, WEIGHT_LIMIT);
        assertThat(elevator.getElevatorCar().getCargoWeight()).isEqualTo(weight * 3);
        assertThat(elevator.isAlarm()).isEqualTo(true);
        assertThat(elevator.isStopEngine()).isEqualTo(true);

        elevatorRequestHandler.load(elevator, -weight);
        assertThat(elevator.getElevatorCar().getCargoWeight()).isEqualTo(weight * 2);
        assertThat(elevator.isAlarm()).isEqualTo(false);
        assertThat(elevator.isStopEngine()).isEqualTo(false);
    }

    @Test
    void call_emptyRequests_shouldAddNewRequest() {
        Elevator elevator = elevatorBuilder.build();
        assumeTrue(elevator.getElevatorRequests().isEmpty());
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

        assumeFalse(elevator.getElevatorRequests().isEmpty());
        Floor floor2 = new Floor(5, null);
        elevatorRequestHandler.call(elevator, floor2);

        assertThat(elevator.getElevatorRequests())
                .hasSize(2)
                .contains(entry(floor1.getFloorNumber(), new ElevatorRequestHandler.FloorCallRequest(floor1)))
                .contains(entry(floor2.getFloorNumber(), new ElevatorRequestHandler.FloorCallRequest(floor2)));
    }

}