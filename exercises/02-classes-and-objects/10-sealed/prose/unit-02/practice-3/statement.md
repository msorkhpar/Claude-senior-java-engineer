`TollBooth` holds the page's two-level hierarchy:

- `Vehicle` is sealed and permits `Car`, `Truck` and `Motorcycle`;
- `Car` is `final`;
- `Truck` is itself `sealed` and permits `PickupTruck` and `SemiTruck`;
- `Motorcycle` is `non-sealed`, and the file extends it with a `Scooter`.

Write `static int toll(Vehicle v)` with a single `switch` over the vehicle and
no `default`:

| vehicle | toll |
|---|---|
| `Car` | 5 |
| `PickupTruck` | 7 |
| `SemiTruck` | 4 per axle (`axles()`) |
| any `Motorcycle` | 2 |

Because `Truck` is sealed, its two kinds together cover every truck, and
because `Motorcycle` is non-sealed, a case for it must also cover the classes
that extend it.

**Examples**

- `toll(new Car())` -> `5`
- `toll(new SemiTruck(3))` -> `12`
- `toll(new Motorcycle())` -> `2`
