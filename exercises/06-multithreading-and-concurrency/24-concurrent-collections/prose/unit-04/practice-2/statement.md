Each writer thread of the audit service collects records in a `RecordBuffer` and drains it to
storage in batches of a few thousand. After an outage, a replay pushes hundreds of thousands of
records through the same buffers. A week after the last outage, a heap dump shows every writer's
buffer still holding an array of hundreds of thousands of slots, all of them empty. Decoding a frame,
whose header gives its row count before the rows arrive one by one, also shows the buffer's array
being copied while the rows go in.

Finish the two `TODO`s in `src/main/java/practice/RecordBuffer.java`:

- `prepareFor(incoming)`: called before a frame's rows are added;
- `drain()`: hands back the records added since the last drain, in order, and leaves the buffer empty.

When you're done, a buffer drained after a replay keeps no more than `NORMAL_BATCH` slots, a
normal batch neither reallocates the array nor grows it again, and a frame's rows go in without
the array growing while they are added. About seven lines in total.

The tests read the buffer's array by reflection.
