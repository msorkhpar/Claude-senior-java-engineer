The CI server posts build news to the team channel. When a build is scheduled, its `BuildJob`
hands the build agent two callbacks: one the agent calls for each progress report, and one it
runs when the build is over, often many minutes later. In the old version the channel filled
with lines like `practice.BuildJob$2@6d06d69c: queued`, which named no build and reported every
build as still queued.

Finish the two `TODO`s in `src/main/java/practice/BuildJob.java`:

- `progressCallback()`: posts `<job>: <percent>%`, for example `build #12 on main: 40%`;
- `completionCallback()`: posts `<job>: <status>`, with the status the build has when the
  callback runs.

`<job>` is what the job's own `toString()` returns. About two lines in total.
