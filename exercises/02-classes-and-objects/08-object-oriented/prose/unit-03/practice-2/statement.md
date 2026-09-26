Abstraction in Java comes from **abstract classes** (shared state and partial
implementation) and **interfaces** (contracts, with optional default methods).
A class can extend one abstract class and implement an interface at the same
time: it inherits the concrete methods of the one and the defaults of the
other, and must implement the abstract methods of both.

This is the course's own `Vehicle`, `Drivable` and `Car`, returning their
messages instead of printing them. In `Garage`, write:

- `Vehicle(String brand)` stores the brand; `stop()` is concrete and returns
  `"<brand> vehicle is stopping."` for every vehicle.
- `Drivable.honk()` is a default method returning `"Honk honk!"`.
- `Car(String brand)` implements `start()` (`"<brand> car is starting."`),
  `accelerate()` (`"<brand> car is accelerating."`) and `brake()`
  (`"<brand> car is braking."`). It keeps `stop()` and `honk()` as they are
  inherited: write each message once, where it belongs.

## Examples

```
Vehicle v = new Car("Toyota");   v.start()      -> "Toyota car is starting."
Drivable d = new Car("Toyota");  d.accelerate() -> "Toyota car is accelerating."
                                 d.brake()      -> "Toyota car is braking."
new Car("Toyota").stop()                        -> "Toyota vehicle is stopping."
new Car("Toyota").honk()                        -> "Honk honk!"
```
