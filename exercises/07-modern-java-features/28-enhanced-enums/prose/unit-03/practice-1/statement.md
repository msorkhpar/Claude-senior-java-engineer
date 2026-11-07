The garage's barrier app charges a parked car in parts. Each time it settles a stretch of parked
time, it calls `charge(visit, minutes)` on the car's tariff and books what comes back. A tariff
has a price per started hour and a cap: however long a car stays, its visit never pays more than
the cap in total.

`Garage` already holds the three tariffs with their prices and caps. Finish the `TODO` in
`src/main/java/practice/Garage.java`:

- `Tariff.charge(visit, minutes)`: what this part of the visit costs, every started hour at the
  tariff's price, cut down to what is left of the visit's cap.

You may add to `Visit` whatever a visit needs. When you're done, every visit pays its started
hours until it reaches its tariff's cap and nothing after that, whether it is the only car in the
garage, follows a car that already hit the cap, or is parked at the same time as other cars on
the same tariff. About six lines in total.
