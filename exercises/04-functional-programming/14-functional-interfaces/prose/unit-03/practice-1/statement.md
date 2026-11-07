The greenhouse dashboard reads its sensors from a message queue in batches. Since the queue started
delivering more than one reading per sensor in a batch, the dashboard page fails to load; and since a
new sensor was installed without calibration, it fails for every batch that contains that sensor. When
it does load, the operators sometimes see a vent at an angle it left minutes ago.

`Readings.dashboard(...)` already formats the page. Finish the two `TODO`s in
`src/main/java/practice/Readings.java`:

- `latestBySensor(...)`: one entry per sensor, the reading that was taken last;
- `unitBySensor(...)`: one entry per sensor that has a unit; an uncalibrated sensor has none.

A batch can hold any number of readings of a sensor, in any order. About six lines in total.
