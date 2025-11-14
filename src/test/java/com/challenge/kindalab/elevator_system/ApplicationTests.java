package com.challenge.kindalab.elevator_system;

import com.challenge.kindalab.elevator_system.domain.Building;
import com.challenge.kindalab.elevator_system.domain.Elevator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.challenge.kindalab.elevator_system.domain.Elevator.ElevatorType.FREIGHT;
import static com.challenge.kindalab.elevator_system.domain.Elevator.ElevatorType.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

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
        assertThat(actual.getBody()[0].getWeightLimit()).isEqualTo(1000);

        assertThat(actual.getBody()[1].getId()).isEqualTo(1);
        assertThat(actual.getBody()[1].getType()).isEqualTo(FREIGHT);
        assertThat(actual.getBody()[1].getWeightLimit()).isEqualTo(3000);
    }

}
