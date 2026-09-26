Constructors are not inherited: a subclass constructor starts by calling a
superclass constructor with `super(...)`. The page lists **forgetting to call
`super(...)`** as a pitfall. If you leave it out, Java quietly calls the no-argument
constructor instead, and the superclass's own checks are skipped. `protected`
members of the superclass are visible to the subclass.

Write `Vehicle` and its nested static subclass `Vehicle.Truck`:

`Vehicle`:

- `protected Vehicle()` makes an unregistered vehicle, whose plate is `UNREGISTERED`;
- `Vehicle(String plate)` refuses a `null` or blank plate with
  `IllegalArgumentException`;
- `getPlate()`, and a `protected` field or getter subclasses may use.

`Truck(String plate, int capacityTonnes)` extends `Vehicle`:

- it is registered with the plate through the superclass's checks;
- a capacity below `1` is refused with `IllegalArgumentException`;
- `getCapacity()`, and `describe()` returns `Truck <plate> (<capacity>t)`.

**Examples**

```
new Vehicle.Truck("AB-123", 12).describe()  -> "Truck AB-123 (12t)"
new Vehicle.Truck(" ", 12)                  -> IllegalArgumentException
new Vehicle.Truck("AB-123", 0)              -> IllegalArgumentException
```
