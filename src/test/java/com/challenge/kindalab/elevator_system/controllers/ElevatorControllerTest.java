package com.challenge.kindalab.elevator_system.controllers;

import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Elevator;
import com.challenge.kindalab.elevator_system.domain.Floor;
import com.challenge.kindalab.elevator_system.handlers.ElevatorRequestHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.TreeMap;
import java.util.UUID;

import static com.challenge.kindalab.elevator_system.domain.Elevator.ElevatorBuilder;
import static com.challenge.kindalab.elevator_system.domain.Elevator.Type.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElevatorController.class)
public class ElevatorControllerTest {

    private static final int ANY_WEIGHT = 123;
    private static final int GROUND_FLOOR_NUMBER = 0;

    private final Floor floor3 = new Floor(3, null);
    private final Elevator.Cabin.CabinBuilder cabinBuilder = Elevator.Cabin.builder().floorNumber(GROUND_FLOOR_NUMBER).weight(ANY_WEIGHT);
    private final ElevatorBuilder elevatorBuilder = Elevator.builder().id(0).type(PUBLIC).cabin(cabinBuilder.build());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Building mockBuilding;

    @MockitoBean
    private ElevatorRequestHandler mockRequestHandler;

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(mockBuilding, mockRequestHandler);
    }

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
    void call_whenIdNotExist_shouldThrowNotFoundException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.emptyList());

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/call/{floorNumber}", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Elevator not found, please check provided id: %s", elevator.getId());
        verify(mockBuilding).getElevators();
    }

    @Test
    void call_whenFloorNumberNotExist_shouldThrowNotFoundException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.emptyMap()));

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/call/{floorNumber}", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Floor not found, please check provided floorNumber: %s", floor3.getFloorNumber());
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
    }

    @Test
    void call_shouldAddAnElevatorRequest() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.singletonMap(floor3.getFloorNumber(), floor3)));

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/call/{floorNumber}", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isOk()).andReturn();
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
        verify(mockRequestHandler).call(elevator, floor3);
    }

    @Test
    void load_whenIdNotExist_shouldThrowNotFoundException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.emptyList());

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/load/{floorNumber}?weight={weight}", elevator.getId(), floor3.getFloorNumber(), ANY_WEIGHT))
                .andExpect(status().isNotFound()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Elevator not found, please check provided id: %s", elevator.getId());
        verify(mockBuilding).getElevators();
    }


    @Test
    void load_whenFloorNumberNotExist_shouldThrowNotFoundException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.emptyMap()));

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/load/{floorNumber}?weight={weight}", elevator.getId(), floor3.getFloorNumber(), ANY_WEIGHT))
                .andExpect(status().isNotFound()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Floor not found, please check provided floorNumber: %s", floor3.getFloorNumber());
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
    }

    @Test
    void load_whenElevatorCabinIsOnADifferentFloor_shouldThrowConflictException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.singletonMap(floor3.getFloorNumber(), floor3)));

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/load/{floorNumber}?weight={weight}", elevator.getId(), floor3.getFloorNumber(), ANY_WEIGHT))
                .andExpect(status().isConflict()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Elevator Cabin isn't on this floor yet, please use floorNumber: %s", cabinBuilder.build().getFloorNumber());
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
    }

    @Test
    void move_whenIdNotExist_shouldThrowNotFoundException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.emptyList());

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/move", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isNotFound()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Elevator not found, please check provided id: %s", elevator.getId());
        verify(mockBuilding).getElevators();
    }

    @Test
    void move_whenRequestIsEmpty_shouldThrowNotFoundException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/move", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isConflict()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("There are not pending requests for Elevator, you can add request by using elevator call or elevator destination APIs");
        verify(mockBuilding).getElevators();
    }

    @Test
    void destination_whenIdNotExist_shouldThrowNotFoundException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.emptyList());

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/destination/{floorNumber}", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isNotFound()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Elevator not found, please check provided id: %s", elevator.getId());
        verify(mockBuilding).getElevators();
    }

    @Test
    void destination_whenFloorNumberNotExist_shouldThrowNotFoundException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.emptyMap()));

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/destination/{floorNumber}", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isNotFound()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Floor not found, please check provided floorNumber: %s", floor3.getFloorNumber());
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
    }

    @Test
    void destination_whenIsFreightElevator_shouldCallRequestHandler() throws Exception {
        Elevator elevator = elevatorBuilder.type(Elevator.Type.FREIGHT).build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.singletonMap(floor3.getFloorNumber(), floor3)));
        doNothing().when(mockRequestHandler).call(elevator, floor3);

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/destination/{floorNumber}", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isOk()).andReturn();
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
        verify(mockRequestHandler).call(elevator, floor3);
    }

    @Test
    void destination_whenFloorKeycardIsNull_shouldCallRequestHandler() throws Exception {
        Elevator elevator = elevatorBuilder.type(Elevator.Type.FREIGHT).build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.singletonMap(floor3.getFloorNumber(), floor3)));
        doNothing().when(mockRequestHandler).call(elevator, floor3);

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/destination/{floorNumber}", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isOk()).andReturn();
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
        verify(mockRequestHandler).call(elevator, floor3);
    }

    @Test
    void destination_whenRequiredKeycardNotProvided_shouldThrownException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        floor3.setKeycard(UUID.randomUUID().toString());
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.singletonMap(floor3.getFloorNumber(), floor3)));

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/destination/{floorNumber}", elevator.getId(), floor3.getFloorNumber()))
                .andExpect(status().isConflict()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Keycard is required to reach the floor: %s", floor3.getFloorNumber());
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
    }

    @Test
    void destination2_whenNotCorrectKeyCardIsProvided_shouldThrownException() throws Exception {
        Elevator elevator = elevatorBuilder.build();
        given(mockBuilding.getElevators()).willReturn(Collections.singletonList(elevator));
        floor3.setKeycard(UUID.randomUUID().toString());
        given(mockBuilding.getFloors()).willReturn(new TreeMap<>(Collections.singletonMap(floor3.getFloorNumber(), floor3)));
        UUID provided = UUID.randomUUID();
        assumeThat(floor3.getKeycard()).isNotEqualTo(provided.toString());

        MvcResult actual = mockMvc.perform(put("/elevators/{id}/destination/{floorNumber}?keycard={keycard}", elevator.getId(), floor3.getFloorNumber(), provided.toString()))
                .andExpect(status().isForbidden()).andReturn();
        assertThat(actual.getResponse().getErrorMessage()).isEqualTo("Provided keycard does match, please check keycard for floorNumber: %s", floor3.getFloorNumber());
        verify(mockBuilding).getElevators();
        verify(mockBuilding).getFloors();
    }

}
