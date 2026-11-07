The dashboard draws a gridline at every tick of a chart's value axis. Users have reported axes
whose top gridline is missing, and on long axes a hover tooltip that shows 61.999999999999766
where the label says 62.00.

Finish the `TODO` in `src/main/java/practice/Axis.java`: `ticks(min, max, step)` returns the tick
values from `min` to `max`, both included, `step` apart. The chart only asks for axes where
`max - min` is a whole number of steps.

When you're done, every axis has the right number of ticks, and each tick is as close to its exact
decimal value as one rounding allows. About five lines in total.
