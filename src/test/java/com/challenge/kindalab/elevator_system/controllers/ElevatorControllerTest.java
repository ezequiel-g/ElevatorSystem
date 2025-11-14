package com.challenge.kindalab.elevator_system.controllers;

import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import com.challenge.kindalab.elevator_system.handlers.ElevatorRequestHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.TreeMap;

import static com.challenge.kindalab.elevator_system.domain.Elevator.ElevatorBuilder;
import static com.challenge.kindalab.elevator_system.domain.Elevator.ElevatorType.PUBLIC;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElevatorController.class)
public class ElevatorControllerTest {

    private final ElevatorBuilder elevatorBuilder = Elevator.builder().type(PUBLIC);
    private final Floor floor3 = new Floor(3, null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Building mockBuilding;

    @MockitoBean
    private ElevatorRequestHandler mockRequestHandler;

    @Test
    void getElevators_shouldReturnAListOfElevators() throws Exception {
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevatorBuilder.build()));
        mockMvc.perform(get("/elevators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is(PUBLIC.toString())));
        verify(mockBuilding).getElevators();
    }

    @Test
    void callElevator_shouldAddAnElevatorRequest() throws Exception {
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevatorBuilder.build()));
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.singletonMap(floor3.getFloorNumber(), floor3)));
        mockMvc.perform(put("/elevators/{id}/call/{floorNumber}", 0, floor3.getFloorNumber()))
                .andExpect(status().isOk());
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
    }

}
