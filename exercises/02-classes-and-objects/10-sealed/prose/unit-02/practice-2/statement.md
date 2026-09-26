A permitted subclass decides how the hierarchy goes on below it: a `final`
subclass ends it, a `sealed` one continues it with its own permitted list, and
a `non-sealed` one opens it to any class. The page's example:

```text
sealed class Vehicle permits Car, Truck, Motorcycle
final class Car extends Vehicle
sealed class Truck extends Vehicle permits PickupTruck, SemiTruck
non-sealed class Motorcycle extends Vehicle
```

Write `static List<String> leaves(Class<?> root)` that lists every kind a
value of the sealed type `root` can be, using `Class.getPermittedSubclasses()`:

- walk the permitted subclasses in the order the `permits` clause names them;
- a `final` subclass is listed by its simple name;
- a `sealed` subclass is not listed itself: its own leaves are listed in its place;
- a `non-sealed` subclass is listed by its simple name followed by `+`, since
  any class may extend it;
- if `root` is not sealed, throw `IllegalArgumentException`.

**Examples**

- `sealed interface Signal permits Red, Amber, Green` (all records) -> `[Red, Amber, Green]`
- the `Vehicle` above -> `[Car, PickupTruck, SemiTruck, Motorcycle+]`
- `leaves(String.class)` -> throws `IllegalArgumentException`
