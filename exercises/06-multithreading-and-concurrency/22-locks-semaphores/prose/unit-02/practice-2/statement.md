The PDF render farm has a fixed number of renderers, shared by two kinds of work. Interactive
previews come from users looking at a spinner. Batch documents come from the nightly scheduler,
which retries anything refused a few minutes later and runs on a small thread pool of its own.

Last night a slow font server made every render take a minute. The scheduler's threads all ended
up parked inside `batch(...)`, so it stopped scheduling anything, including the jobs that don't
render at all. Users complained too: some previews spun until the browser gave up, and others
were turned away while documents nobody was looking at were being rendered.

Finish `admitPreview()` and `admitBatch()` in `src/main/java/practice/RenderAdmission.java`.
`preview(...)` and `batch(...)` already render and give the renderer back. When you're done, a
preview waits for a renderer, but no longer than `previewWaitMillis`; a batch document never
waits, and never takes a renderer that another caller is already queued for. About two lines in
total.

The tests hand the class its semaphore, so they can hold renderers, queue callers, and play a
second scheduler that takes a renderer at an awkward moment.
