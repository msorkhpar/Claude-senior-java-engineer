Every `ElectricCar` is also a `Car`, so `vehicle instanceof Car car` matches electric cars
too. In a chain of patterns over a hierarchy, the subclass is tested first; otherwise its
branch is never reached.

`Garage` holds a small hierarchy: `Vehicle`, `Car implements Vehicle` (`drive()`),
`ElectricCar extends Car` (`charge()` as well) and `Bicycle implements Vehicle` (`pedal()`).
Write `use(Vehicle vehicle)` in `Garage`. It returns what the vehicle does:

- an electric car: `drive()` and `charge()` joined by `+`, that is `"drive+charge"`;
- any other car: `"drive"`;
- a bicycle: `"pedal"`;
- anything else, `null` included: `"unknown"`.
