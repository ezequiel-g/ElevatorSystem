package com.challenge.kindalab.ElevatorSystem.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
class BuildingsController {

    @GetMapping("/buildings")
    List<String> getBuildings() {
        return Collections.emptyList();
    }

}
