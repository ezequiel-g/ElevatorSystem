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

* GET /building

### Elevator Controller

You can use it to get elevator info and control it.

* GET /elevators
    * Returns a list of elevators
* PUT /elevators/{id}/load/{floorNumber}?weight=300
    * Simulate adding weight to the Elevator Cabin, if accumulation of added weight exceed limit, it will stop engine
      and
      start alarm
        * Negative weight will simulate removing weight from Elevator Cabin
* PUT /elevators/{id}/call/{floorNumber}
    * Simulate calling the Elevator Cabin from a floor. Once the Elevator Cabin reach the floor it can be loaded.
* PUT /elevators/{id}/destination/{floorNumber}?keycard=keycard1
    * Simulate choosing floor to go to from into the Elevator Cabin a keycard may be required in public elevator
* PUT /elevators/{id}/move
    * Simulate Elevator Cabin moving between floors, depending on calls and destinations




