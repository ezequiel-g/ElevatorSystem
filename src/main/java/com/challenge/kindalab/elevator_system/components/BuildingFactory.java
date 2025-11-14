package com.challenge.kindalab.elevator_system.components;

import com.challenge.kindalab.elevator_system.config.BuildingConfig;
import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

@Component
public class BuildingFactory {

    private final int GROUND_FLOOR_NUMBER = 0;

    private final BuildingConfig buildingConfig;
    private final ElevatorFactory elevatorFactory;

    @Autowired
    public BuildingFactory(BuildingConfig buildingConfig, ElevatorFactory elevatorFactory) {
        this.buildingConfig = buildingConfig;
        this.elevatorFactory = elevatorFactory;
    }

    /**
     * Build a building base on the buildConfig, using British conventions.
     *
     * @return a Building
     */
    @Bean
    public Building build() {
        NavigableMap<Integer, Floor> floors = new TreeMap<>(Collections.singletonMap(GROUND_FLOOR_NUMBER, new Floor(GROUND_FLOOR_NUMBER, null)));
        addBasementFloors(floors);
        addRegularFloors(floors);
        setKeyCards(floors);

        List<Elevator> elevators = elevatorFactory.build(buildingConfig.getElevatorConfigs());
        return Building.builder()
                .floors(floors)
                .elevators(elevators)
                .build();
    }

    private void setKeyCards(NavigableMap<Integer, Floor> floors) {
        buildingConfig.getKeycards().forEach(
                (k, v) -> floors.get(k)
                        .setKeycard(v)
        );
    }

    private void addBasementFloors(NavigableMap<Integer, Floor> floors) {
        for (int i = GROUND_FLOOR_NUMBER - 1; i >= -buildingConfig.getBasementFloorsQuantity(); i--) {
            floors.put(i, new Floor(i, null));
        }
    }

    private void addRegularFloors(NavigableMap<Integer, Floor> floors) {
        for (int j = GROUND_FLOOR_NUMBER + 1; j < buildingConfig.getRegularFloorsQuantity(); j++) {
            floors.put(j, new Floor(j, null));
        }
    }

}