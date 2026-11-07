The tank farm's `TankLevel` keeps the level its sensor last reported and passes every reading to
its subscribers: the control panel, the overflow alarm, the pump controllers. Each of those
belongs to another team, takes its own locks, and may call back into the tank.

Three incidents came from the same method. The control panel froze, and its thread dump showed the
sensor thread waiting for the panel while the panel waited for the tank. A pump controller that
subscribes on its first reading made that delivery fail halfway. And the overflow alarm missed a
spike of 25,400 litres, because the overflow valve had already reported the level after opening.

Write `update(...)` in `src/main/java/practice/TankLevel.java`. It records the reading and passes
it to every subscriber, which hears each reading it is told about, with that reading's value. Nothing a
subscriber does can freeze the tank for its other callers or break a delivery in progress. About six lines in
total.

The tests run the sensor on daemon threads and ask the JVM's deadlock detector what is stuck, so a
deadlock fails a test instead of hanging it.
