The page's Q4 maps `Vehicle`, `Car` and `Truck` with `SINGLE_TABLE`: one
table for the whole hierarchy, a discriminator column naming each row's
class, and the subclass columns NULL on the other type's rows. Write that
mapping by hand, with a sealed interface and records.

The table is
`vehicles(id BIGINT PRIMARY KEY, vehicle_type VARCHAR, manufacturer VARCHAR, doors INT, payload DECIMAL(10,2))`.
In `Vehicles`:

- `save(connection, vehicle)` inserts a `Car` with `vehicle_type` `CAR` or a
  `Truck` with `TRUCK`. **A column of the other type is written as NULL, not
  0.**
- `findAll(connection)` returns every vehicle in id order. **The
  `vehicle_type` column, not which columns are NULL, decides the class**; a
  NULL doors gives `null`. **An unknown `vehicle_type` is refused** with
  `IllegalStateException`.

| row (id, vehicle_type, manufacturer, doors, payload) | loaded as |
|---|---|
| 1, CAR, Volvo, 4, NULL | `Car[id=1, manufacturer=Volvo, doors=4]` |
| 2, TRUCK, Scania, NULL, 12000.50 | `Truck[id=2, manufacturer=Scania, payload=12000.50]` |
| 3, TRUCK, MAN, NULL, NULL | `Truck[id=3, manufacturer=MAN, payload=null]` |
| 5, CAR, Fiat, NULL, NULL | `Car[id=5, manufacturer=Fiat, doors=null]` |
| 4, BUS, Setra, NULL, NULL | `IllegalStateException` |
