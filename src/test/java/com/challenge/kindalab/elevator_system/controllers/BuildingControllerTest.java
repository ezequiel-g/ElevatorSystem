package com.challenge.kindalab.elevator_system.controllers;

import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Floor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuildingController.class)
class BuildingControllerTest {

    private static final int GROUND_FLOOR_NUMBER = 0;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Building mockBuilding;

    @Test
    void getBuilding() throws Exception {
        Floor groundFloor = new Floor(GROUND_FLOOR_NUMBER, null);
        NavigableMap<Integer, Floor> floors = new TreeMap<>(Collections.singletonMap(groundFloor.getFloorNumber(), groundFloor));
        given(mockBuilding.getFloors()).willReturn(floors);

        MvcResult actual = mockMvc.perform(get("/building")).andExpect(status().isOk())
                .andExpect(jsonPath("floors", aMapWithSize(floors.size()))).andReturn();
    }

}