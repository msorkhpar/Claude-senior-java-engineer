An image service decodes into buffers outside the Java heap. `NativeBuffer` takes its memory from
an `Allocator` and must give the address back with `free`. Most code closes its buffers with
try-with-resources, but a few code paths forget, and the service's native memory climbs until the
container is killed. Freeing an address twice is worse: it corrupts the allocator.

Finish the two `TODO`s in `src/main/java/practice/NativeBuffer.java`:

- `register(cleaner)`: what frees the buffer's memory if nobody closes it;
- `close()`: free the memory now.

When you're done, every buffer's address is freed exactly once: at `close()` if it is closed, and
otherwise after a garbage collection finds the buffer unreachable. About four lines in total.

The tests give each buffer its own `Cleaner`, ask the JVM for collections with `System.gc()`, and
wait up to ten seconds for a forgotten buffer to be freed.
