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
    * Simulate adding weight to the elevatorCar, if accumulation of added weight exceed limit, it will stop engine and
      start alarm
        * Negative weight will simulate removing weight from elevatorCar
* PUT /elevators/{id}/call/{floorNumber}
    * Simulate calling the elevator from a floor. Once the elevateCar reach the floor it can be loaded.
* PUT /elevators/{id}/destination/{floorNumber}?keycard=keycar1
    * Simulate choosing floor to go to from into the elevatorCar a keycard may be required in public elevator
* PUT /elevators/{id}/move/
    * Simulate elevatorCar moving between floors, depending on calls and destinations




