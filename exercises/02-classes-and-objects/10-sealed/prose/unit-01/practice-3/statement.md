The page's syntax section seals an interface as well as a class:
`public sealed interface Vehicle permits Car, Motorcycle, Truck { }`. In the
course's code the three kinds are records, and a record is implicitly `final`,
so it already has one of the modifiers a permitted subtype needs.

`Garage` holds the three records and an interface `Vehicle` that anyone could
still implement.

1. Seal `Vehicle` so that exactly `Car`, `Motorcycle` and `Truck` may
   implement it.
2. Write `static int wheels(Vehicle v)`: a car has 4, a motorcycle 2, a truck 6.
3. Write `static long tollCents(Vehicle v)`:
   - a car pays 250;
   - a motorcycle pays 100;
   - a truck pays 500, plus 100 for every **full** 1000 kg of its `capacity`.

Use `switch` expressions over the vehicle; with `Vehicle` sealed, neither
needs a `default`.

**Examples**

- `wheels(new Car("Tesla"))` -> `4`, `tollCents(new Car("Tesla"))` -> `250`
- `tollCents(new Motorcycle("Harley-Davidson"))` -> `100`
- `tollCents(new Truck(10000))` -> `1500`
- `tollCents(new Truck(2500))` -> `700`
