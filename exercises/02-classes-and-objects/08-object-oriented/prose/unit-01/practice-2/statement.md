Constructors are not inherited, and every constructor runs its superclass's
constructor **first**, through an explicit or implicit `super(...)`. A
constructor may instead hand over to another constructor of the same class
with `this(...)`. Building an object therefore runs a **chain** of
constructors, from the top of the hierarchy down.

In `Chain`, each constructor records one line in the shared `log` it is
given, **after** the constructor it chains to has finished:

- `Vehicle(String brand, List<String> log)` stores the brand in its
  `protected` field and records `"Vehicle <brand>"`.
- `Car(String brand, int doors, List<String> log)` stores the doors in its
  `protected` field and records `"Car <doors> doors"`.
- `Car(String brand, List<String> log)` is a four-door car: it hands over to
  the full constructor with `this(...)` and records nothing of its own.
- `SportsCar(String brand, List<String> log)` is a two-door `Car` and records
  `"SportsCar"`.
- `SportsCar.describe()` returns `"<brand> sports car with <doors> doors"`,
  read from the fields its superclasses set.

## Examples

```
new SportsCar("Ferrari", log)  -> log ["Vehicle Ferrari", "Car 2 doors", "SportsCar"]
  .describe()                  -> "Ferrari sports car with 2 doors"
new Car("Fiat", log)           -> log ["Vehicle Fiat", "Car 4 doors"]
```
