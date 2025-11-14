package com.challenge.kindalab.elevator_system.config;

import lombok.Builder;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.List;
import java.util.Map;

@Builder
@Value
@ConfigurationProperties("building")
@ConfigurationPropertiesScan
public class BuildingConfig {

    int basementFloorsQuantity;
    int regularFloorsQuantity;
    Map<Integer, String> keycards;
    List<ElevatorConfig> elevatorConfigs;

}