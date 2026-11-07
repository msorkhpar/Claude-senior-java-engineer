Request handlers in the order service hand background work to `RequestPool`, and every log line
the service writes is tagged with `RequestPool.REQUEST_ID`, the id of the request the current
thread is serving. Lines written by pool tasks come out with no request id at all, so nobody can
follow an order from its handler into the work it started. Handlers submit both `Callable`s and
`Runnable`s, and housekeeping jobs, which serve no request, use `execute(...)`. When the pool and
its queue are full, the handler's own thread runs the task, and carries on serving its request
afterwards.

Finish `RequestPool` in `src/main/java/practice/RequestPool.java` so that every task a handler
submits logs under that handler's request id, and nothing else logs under it: not the
housekeeping that runs on the same thread later, and not the handler, which keeps its own id.
The handlers' code doesn't change. About fifteen lines in total.
