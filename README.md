# Getting Started

### Minimums requirements

* jdk 17
* maven 3.9

### Run application

    mvn clean spring-boot:run

### Swagger url

    http://localhost:8080/swagger-ui.html

### Building Controller
You can use it to get building info.

**Floors info.**

* ground floor {id: 0, keycard: null}
* basement floor {id: -1, keycard: 716a48c7-7b1d-4a5e-8b1d-c6d37d43ba9f }
* last floor {id: 49, keycard: dc4e7ba5-376b-4964-a6a8-6b66f9498a45 }

---

* GET /building

### Elevator Controller
You can use it to get elevator info and control it.

**Elevators info.**

* PUBLIC elevator id: 0
* FREIGHT elevator id: 1

---

* GET /elevators
    * Returns a list of elevators
* PUT /elevators/{id}/call/{floorNumber}
    * Simulate calling the Elevator Cabin from a floor. Once the Elevator Cabin reach the floor it can be loaded.
* PUT /elevators/{id}/load/{floorNumber}?weight=300
    * Simulate adding weight to the Elevator Cabin, if accumulation of added weight exceed limit, it will stop the
      engine
      and start the alarm
        * Negative weight will simulate removing weight from Elevator Cabin.
* PUT /elevators/{id}/destination/{floorNumber}?keycard=keycard1
    * Simulate choosing floor to go to from into the Elevator Cabin a keycard may be required in public elevator.
* PUT /elevators/{id}/move
    * Simulate Elevator Cabin moving between floors, depending on calls and destinations.
        * Elevator default movement is moving up.
        * Once the elevator reached the last destination in one direction you need to do a move again




