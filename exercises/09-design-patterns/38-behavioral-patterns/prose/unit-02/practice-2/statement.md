This practice continues the previous one: `BuildEvents.publish` already walks the plug-ins safely
when one of them unsubscribes itself, and now hands each build to each plug-in through
`deliver(listener, buildId)`.

Plug-ins are written by other teams. Last night the artifact-cleaner plug-in threw on every build
because an agent's disk was full. Every plug-in registered after it stopped hearing about builds,
and the code that calls `publish` at the end of a build marked green builds as failed. Nobody
running the server could tell which plug-in was at fault.

`BuildEvents` now takes an `onPluginFailure` handler in its constructor. Finish
`deliver(listener, buildId)` in `src/main/java/practice/BuildEvents.java` so that a plug-in that
throws costs no other plug-in its build, never makes `publish` fail, and is reported to that
handler with the build id and the exception it threw. About five lines in total.
