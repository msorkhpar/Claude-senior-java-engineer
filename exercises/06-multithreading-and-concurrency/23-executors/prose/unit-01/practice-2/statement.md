The nightly settlement run reported success, and in the morning three merchants had not been
paid. The jobs that should have paid them had thrown, and nobody heard about it. The callers that
hand jobs to `SettlementPool` are spread over a dozen services: some use `execute()`, some use
`submit()`, and most ignore whatever comes back. Changing them all is not an option.

`SettlementPool` already passes every failure it finds to `Alerts` from its `afterExecute` hook.
Finish `failureOf(job, thrown)` in `src/main/java/practice/SettlementPool.java`, so that every job
that fails raises one alert, however it was handed over, and the alert carries the exception the
job itself threw. An operator sometimes cancels a rerun while it is running; that is not a failure,
and must not cost the pool its worker. About ten lines in total.

The tests use a pool with one worker and wait for it to finish before they read the alerts.
