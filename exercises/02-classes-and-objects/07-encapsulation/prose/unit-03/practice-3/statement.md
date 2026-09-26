The page's strongest protection is an **immutable object**: once made, its state
never changes, so there is nothing to guard. The page also warns that
**subclassing can break encapsulation**: a subclass could add mutable state or
override behaviour, so an immutable class is declared `final`.

Write `Schedule`, an immutable list of time slots with a name:

- `Schedule(String name, List<String> slots)`;
- `String getName()` and `List<String> getSlots()`: the slots can be read but not
  changed through the returned list;
- `Schedule withSlot(String slot)` returns a **new** schedule with `slot` added at
  the end. The schedule it was called on stays as it was.

Changing the list passed to the constructor afterwards does not change the schedule.
The class cannot be extended, and every field it declares is `private` and `final`.

Examples:

```
Schedule monday = new Schedule("Mon", List.of("09:00"));
Schedule longer = monday.withSlot("14:00");
longer.getSlots()     -> [09:00, 14:00]
monday.getSlots()     -> [09:00]
```
