The billing service keeps one schema per tenant. Its jobs run on a shared thread pool, and the
repositories call `TenantContext.current()` to choose the schema. Last month an invoice run for a
small tenant wrote three invoices into another tenant's schema. The job that ran just before it on
the same pool thread had failed halfway with "ledger locked". Transfers between two tenants run one
job inside another: the outer job for the payer, a nested one for the payee.

Write `runAs(...)` in `src/main/java/practice/TenantContext.java`. When you're done, a job sees
its own tenant while it runs, and every job after it on the same thread sees what it should: no
tenant after a job, even after a failed one, and the outer tenant again when a nested job ends. The
job's exception still reaches the caller. About ten lines in total.

The tests run the jobs on a pool with a single thread, so each job runs on the thread the one
before it used.
