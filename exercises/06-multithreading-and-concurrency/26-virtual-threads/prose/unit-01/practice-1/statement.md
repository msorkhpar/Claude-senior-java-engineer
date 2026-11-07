The reporting service moved to virtual threads last month: the web server now runs every request
on a virtual thread of its own and calls `ReportService.handle(...)` there. Since then, whenever
finance pulls a batch of quarterly reports, the service's liveness probe times out, the
orchestrator restarts the instance, and ordinary requests that render nothing see their latency
jump. Rendering a report is pure computation, with no I/O: the `Renderer` you are given takes tens
to hundreds of milliseconds of CPU per report.

Finish the two `TODO`s in `src/main/java/practice/ReportService.java`:

- `newRenderPool(cores)`: called once, from the constructor;
- `render(reportId)`: produces one report for the calling request.

When you're done, a batch of reports can no longer hold up the service's other requests, rendering
never uses more than the `cores` the service was given, and when that many reports are requested
together, they all render at once. About two lines in total.

The tests send each request on a virtual thread of its own, as the web server does, and hold
renders in progress to see where and how many run at once.
