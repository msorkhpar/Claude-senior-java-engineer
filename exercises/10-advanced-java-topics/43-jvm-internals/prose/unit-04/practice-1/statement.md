`-Xmx` and `-Xms` set the maximum and initial heap, and `-Xss` each thread's
stack. In a container, `-XX:MaxRAMPercentage=75.0` sizes the heap as a share
of the container's memory limit instead. Sizes take a suffix `k`, `m` or `g`
(**powers of 1024, upper or lower case**); a bare number is bytes.

Write `HeapSizing.parse(args, containerLimitBytes)` that answers:

- `maxHeapBytes()`: **`-Xmx` when given**, otherwise `MaxRAMPercentage`
  (default `25.0`) percent of the container limit, rounded down.
- `threadStackBytes()`: `-Xss`, default 1 MiB.

Other arguments are ignored, and when a flag appears twice **the last one
wins**. An `-Xms` **larger than `-Xmx`** is refused with
`IllegalArgumentException`, as the JVM refuses to start with that pair.

| args | container limit | max heap | stack |
|---|---|---|---|
| `-Xmx4g -Xss512k` | 8 GiB | 4 GiB | 512 KiB |
| `-XX:MaxRAMPercentage=75.0` | 8 GiB | 6 GiB | 1 MiB |
| (none) | 8 GiB | 2 GiB | 1 MiB |
| `-Xmx2G -XX:MaxRAMPercentage=75.0` | 8 GiB | 2 GiB | 1 MiB |
| `-Xmx1g -Xmx3g` | 8 GiB | 3 GiB | 1 MiB |
| `-Xms4g -Xmx2g` | 8 GiB | `IllegalArgumentException` | |
