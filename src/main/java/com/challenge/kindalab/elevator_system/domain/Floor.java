package com.challenge.kindalab.elevator_system.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Floor {

    private int floorNumber;
    private String keycard;

}
