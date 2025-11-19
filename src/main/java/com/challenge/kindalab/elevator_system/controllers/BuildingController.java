package com.challenge.kindalab.elevator_system.controllers;

import com.challenge.kindalab.elevator_system.domain.Building;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RequestMapping("building")
@RestController
class BuildingController {

    private final Building building;

    @GetMapping("")
    ResponseEntity<Building> getBuilding() {
        return ResponseEntity.ok(building);
    }

}
