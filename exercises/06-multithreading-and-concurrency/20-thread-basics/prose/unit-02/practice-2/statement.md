The service's `/health` endpoint checks Postgres, Redis, Kafka and S3, each on its own thread, and
the load balancer gives up on the endpoint after two seconds, so the whole check has to fit in that
budget. During last night's Kafka outage two things went wrong. Most requests to `/health`
answered "all passed" after two seconds while Kafka was unreachable. A few never answered at all,
and the load balancer took the instance out of rotation.

`run(...)` starts the probes and builds the report. Finish `await(...)` in
`src/main/java/practice/HealthCheck.java`: the wait for one probe, against the deadline the whole
check shares.

When you're done, a probe still running at the deadline is reported as timed out, and `/health`
answers within its budget however the time left is spread over the probes. About three lines.
