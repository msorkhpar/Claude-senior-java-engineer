A switch of type patterns over a sealed interface can name every permitted type and need no
`default`: the compiler checks the cases are exhaustive, and refuses the switch if a new
vehicle type is added and not handled.

`TollGate` holds `sealed interface Vehicle permits Car, Truck, Motorcycle` and three records:
`Car(int doors)`, `Truck(double cargoCapacity)` and `Motorcycle(boolean hasSidecar)`. Write
`toll(Vehicle vehicle)` in `TollGate` as one switch expression with no `default`:

| vehicle | toll |
|---|---|
| any car | `5` |
| a truck | `10`, plus `2` for each started tonne of capacity: `Truck(3.5)` pays `18` |
| a motorcycle | `2`, or `3` with a sidecar |
