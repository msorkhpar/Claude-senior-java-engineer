This practice continues the billing service's `TenantContext`: its `runAs(...)` is the one you
wrote, already in place.

Billing jobs now send notifications on a separate notifier pool, so that a slow mail server doesn't
hold up an invoice run. The notification code reads `TenantContext.current()` to pick the tenant's
sender address and template. The first attempt sent notifications with no tenant at all; a quick
fix made every notification go out under the name of one tenant, the one whose job happened to run
first after the deploy.

Write `wrap(...)` in `src/main/java/practice/TenantContext.java`. A job calls it on a task before
handing the task to another thread. When you're done, a wrapped task runs as the tenant of the job
that wrapped it, on any thread and whichever job started that thread, and leaves the thread as
it found it. About five lines in total.

The tests run each job on a thread of its own and send its notification through a one-thread pool
that the first job to use it starts.
