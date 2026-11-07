The photo service renders thumbnails for new uploads on a pool of worker threads. On every
deploy the service calls `shutdownNow()` on that pool and waits for it, and on a busy afternoon
the wait runs out: a worker is in the middle of a batch of two thousand images and finishes
every one of them first.

`ThumbnailWorker` already renders correctly. Finish the two `TODO`s in
`src/main/java/practice/ThumbnailWorker.java`:

- `stopRequested()`: whether the worker has been asked to stop;
- the loop in `renderBatch(...)`: a way out between two images.

When you're done, a worker that is asked to stop finishes the image it is rendering and renders
nothing more, in this batch or the next, and the code that called `renderAll` can still tell
that it was asked. About three lines in total.

The tests run the worker on its own thread and on a real `ExecutorService`.
