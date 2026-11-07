A metrics agent keeps the latency samples it records between two flushes in a `SampleBuffer`, a
growable `double[]`. On quiet nodes nobody notices it. On the busiest nodes, which record a few
hundred thousand samples between flushes, the agent falls behind, and a profile shows most of
its time inside `Arrays.copyOf`.

The buffer already stores, copies and counts. Finish `newCapacity(current, needed)` in
`src/main/java/practice/SampleBuffer.java`: the size of the new array when the buffer is full.
A buffer may be created with capacity 0, and `addAll` can bring a batch larger than the room
left.

When you're done, filling a buffer with n samples copies a small multiple of n elements in
total, however large n gets. About three lines in total.
