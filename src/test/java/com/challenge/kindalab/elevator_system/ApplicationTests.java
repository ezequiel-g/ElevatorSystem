package com.challenge.kindalab.elevator_system;

import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Elevator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static com.challenge.kindalab.elevator_system.domain.Elevator.Type.FREIGHT;
import static com.challenge.kindalab.elevator_system.domain.Elevator.Type.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

    private static final int PUBLIC_WEIGHT_LIMIT = 1000;
    private static final int FREIGHT_WEIGHT_LIMIT = 3000;

    @Value("${building.basementFloorsQuantity}")
    private int basementFloorsQuantity;

    @Value("${building.regularFloorsQuantity}")
    private int regularFloorsQuantity;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getBuilding_shouldReturnABuilding() {
        ResponseEntity<Building> actualEntity = restTemplate.getForEntity("/building", Building.class);
        assertThat(actualEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Building actualBuilding = actualEntity.getBody();
        assertThat(actualBuilding).isNotNull();

        assertThat(actualBuilding.getFloors()).hasSize(regularFloorsQuantity + basementFloorsQuantity);
        assertThat(actualBuilding.getElevators()).hasSize(2);
    }

    @Test
    void getElevators_shouldReturnAListOfElevators() {
        ResponseEntity<Elevator[]> actual = restTemplate.getForEntity("/elevators", Elevator[].class);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody())
                .isNotNull()
                .hasSize(2);

        assertThat(actual.getBody()[0].getId()).isEqualTo(0);
        assertThat(actual.getBody()[0].getType()).isEqualTo(PUBLIC);
        assertThat(actual.getBody()[0].getWeightLimit()).isEqualTo(PUBLIC_WEIGHT_LIMIT);

        assertThat(actual.getBody()[1].getId()).isEqualTo(1);
        assertThat(actual.getBody()[1].getType()).isEqualTo(FREIGHT);
        assertThat(actual.getBody()[1].getWeightLimit()).isEqualTo(FREIGHT_WEIGHT_LIMIT);
    }

    @Test
    void load_fromGroundFloorAndEmptyElevatorCabin() {
        loadResetElevatorCabinWeight();

        int weight = 100;
        Map<String, Object> urlVariables = Map.of("id", 0, "floorNumber", 0, "weight", weight);
        ResponseEntity<Elevator> actual = restTemplate.exchange("/elevators/{id}/load/{floorNumber}?weight={weight}", HttpMethod.PUT, HttpEntity.EMPTY, Elevator.class, urlVariables);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody().getCabin().getWeight()).isEqualTo(weight);
    }

    @Test
    void load_fromGroundFloorAddingWeightTwice() {
        loadResetElevatorCabinWeight();

        int weight = 100;
        Map<String, Object> urlVariables = Map.of("id", 0, "floorNumber", 0, "weight", weight);
        ResponseEntity<Elevator> actual = restTemplate.exchange("/elevators/{id}/load/{floorNumber}?weight={weight}", HttpMethod.PUT, HttpEntity.EMPTY, Elevator.class, urlVariables);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody().getCabin().getWeight()).isEqualTo(weight);

        ResponseEntity<Elevator> actual2 = restTemplate.exchange("/elevators/{id}/load/{floorNumber}?weight={weight}", HttpMethod.PUT, HttpEntity.EMPTY, Elevator.class, urlVariables);
        assertThat(actual2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual2.getBody().getCabin().getWeight()).isEqualTo(weight * 2);
    }

    /**
     * By sending a negative weight exceed ing weight limits is like reset Elevator Cabin weight to 0
     */
    private void loadResetElevatorCabinWeight() {
        Map<String, Object> urlVariables1 = Map.of("id", 0, "floorNumber", 0, "weight", -FREIGHT_WEIGHT_LIMIT);
        ResponseEntity<Elevator> actual1 = restTemplate.exchange("/elevators/{id}/load/{floorNumber}?weight={weight}", HttpMethod.PUT, HttpEntity.EMPTY, Elevator.class, urlVariables1);
        assertThat(actual1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual1.getBody().getCabin().getWeight()).isEqualTo(0);
    }

    @Test
    void call_fromThirdFloor_shouldIncludeAnElevatorRequest() {
        Map<String, Object> urlVariables = Map.of("id", 0, "floorNumber", 3);
        ResponseEntity<Elevator> actual = restTemplate.exchange("/elevators/{id}/call/{floorNumber}", HttpMethod.PUT, HttpEntity.EMPTY, Elevator.class, urlVariables);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody().getElevatorRequests()).hasSize(1);
        assertThat(actual.getBody().getElevatorRequests()).isEmpty();
    }

    @Test
    void call_fromThirdFloorTwice_shouldIncludeOnlyOneElevatorRequest() {
        Map<String, Object> urlVariables = Map.of("id", 0, "floorNumber", 3);
        restTemplate.exchange("/elevators/{id}/call/{floorNumber}", HttpMethod.PUT, HttpEntity.EMPTY, Elevator.class, urlVariables);
        ResponseEntity<Elevator> actual = restTemplate.exchange("/elevators/{id}/call/{floorNumber}", HttpMethod.PUT, HttpEntity.EMPTY, Elevator.class, urlVariables);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody().getElevatorRequests()).hasSize(1);
        assertThat(actual.getBody().getElevatorRequests()).isEmpty();
    }

    @Test
    void move() {
        Map<String, Object> urlVariables = Map.of("id", 0);
        ResponseEntity<Elevator> actual = restTemplate.exchange("/elevators/{id}/move", HttpMethod.PUT, HttpEntity.EMPTY, Elevator.class, urlVariables);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody().getCabin().getFloorNumber()).isEqualTo(3);
    }

    @Disabled
    @Test
    void destination() {
        // TODO
    }

}
