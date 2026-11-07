The nightly archive job compresses the day's uploads on a `ForkJoinPool`. Compressing a file takes
time in proportion to its bytes, and the files range from a few bytes to several gigabytes. The
job used to cut the list of files in half by count until a batch held a handful of files, and one
worker ended up with the batch of video files while the others had finished long before.

`ArchiveJob` now gives each task a budget in bytes. Finish the two `TODO` methods in
`src/main/java/practice/ArchiveJob.java`:

- `isLeaf(from, to)`: whether one task compresses the files `[from, to)`;
- `splitPoint(from, to)`: where a batch too big for one task is cut.

When you're done, every batch a task compresses fits the budget, and each cut leaves neither half
with more than half the batch's bytes plus the batch's largest file. Some uploads are bigger than
the whole budget on their own. `bytesIn(from, to)` is there to use. About eight lines in total.
