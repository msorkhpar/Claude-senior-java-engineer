The file-transfer dashboard shows two figures: the total bytes moved today and the mean duration
of the most recent transfers. On the day the team mirrored three large disk images, the total
went negative. On a quiet night with no transfers at all, the dashboard said the recent transfers took
0.0 ms on average, which paged nobody but convinced someone the service was fast.

Finish the two `TODO` methods in `src/main/java/practice/TransferStats.java`:

- `totalBytes(sizes)`: the total of all the sizes;
- `meanOfLast(durationsMs, n)`: the mean of the last `n` durations (all of them if there are
  fewer), and nothing when there are none, so the dashboard can show `n/a`.

The sizes and durations arrive as `int[]`. About four lines in total.
