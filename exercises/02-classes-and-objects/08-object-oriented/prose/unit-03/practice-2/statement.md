The monitoring charts can't draw millions of points, so `Downsampler` keeps every `step`-th sensor
reading: positions 0, `step`, 2·`step`, and so on. Two callers use it. The live dashboard passes
buffers of four million readings and keeps one in a thousand. The archive job passes readings it
replays from disk into a linked list, and keeps one in seven. The first version was correct on
both, and the archive job ran for hours; the second fixed the archive job, and the dashboard
started reading every reading in the buffer to keep a thousandth of them.

Finish the `TODO` in `src/main/java/practice/Downsampler.java`: `pick(readings, step)` returns the
kept readings in order, and stays fast for both callers, and for any other `List` that reaches it.
About eight lines.

The tests count the links a linked list walks, and the readings a buffer is asked for.
