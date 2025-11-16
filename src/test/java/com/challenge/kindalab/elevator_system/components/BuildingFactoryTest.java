package com.challenge.kindalab.elevator_system.components;

import com.challenge.kindalab.elevator_system.config.BuildingConfig;
import com.challenge.kindalab.elevator_system.config.ElevatorConfig;
import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Elevator.Type;
import com.challenge.kindalab.elevator_system.domain.Floor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BuildingFactoryTest {

    private static final String ANY_KEYCARD = "ANY_KEYCARD";
    private static final int GROUND_FLOOR_NUMBER = 0;

    private final int basementFloorsQuantity = 1;
    private final int regularFloorsQuantity = 3;
    private final Map<Integer, String> keycards = Collections.singletonMap(-basementFloorsQuantity, ANY_KEYCARD);

    private final BuildingConfig.BuildingConfigBuilder configBuilder = BuildingConfig.builder()
            .basementFloorsQuantity(basementFloorsQuantity)
            .regularFloorsQuantity(regularFloorsQuantity)
            .keycards(keycards)
            .elevatorConfigs(Collections.singletonList(new ElevatorConfig(Type.PUBLIC, 1)));
    private final ElevatorFactory elevatorFactory = new ElevatorFactory();
    private final BuildingFactory factory = new BuildingFactory(configBuilder.build(), elevatorFactory);

    @Test
    void build() {
        Building actual = factory.build();
        assertThat(actual.getFloors())
                .hasSize(basementFloorsQuantity + regularFloorsQuantity)
                .containsOnlyKeys(-1, 0, 1, 2)
                .containsEntry(-basementFloorsQuantity, new Floor(-basementFloorsQuantity, ANY_KEYCARD))
                .containsEntry(GROUND_FLOOR_NUMBER, new Floor(GROUND_FLOOR_NUMBER, null));
        assertThat(actual.getElevators())
                .hasSize(1);
    }

}