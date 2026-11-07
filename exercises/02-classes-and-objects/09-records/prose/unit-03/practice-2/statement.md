The job runner takes jobs from a broker. When a delivery fails, the runner asks `RetryPolicy`
whether to put the job back, and a job that keeps failing is meant to be dropped after a few
deliveries. Each delivery is read from its message into a new `Job` object. `Job` is a record
that the producer shares, and the first attempt to count deliveries in a field of `Job` didn't
compile.

Finish `failed(...)` and `failures(...)` in `src/main/java/practice/RetryPolicy.java`. A job is
delivered at most `maxDeliveries` times, and its count must not depend on which `Job` object a
delivery arrived in. You may add fields to `RetryPolicy`, but don't change `Job`. About four lines
in total.
