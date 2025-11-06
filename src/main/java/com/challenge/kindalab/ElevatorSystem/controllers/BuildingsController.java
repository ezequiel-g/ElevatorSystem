package com.challenge.kindalab.ElevatorSystem.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class BuildingsController {

    @GetMapping("/buildings")
    public List<String> getBuildings() {
        return Collections.emptyList();
    }

}
