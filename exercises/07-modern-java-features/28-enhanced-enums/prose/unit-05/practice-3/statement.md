The upload service reads one quota per pricing tier from its properties at startup
(`quota.FREE=25`, `quota.TEAM=750`, ...). Last quarter's release added the `ENTERPRISE` tier, and
the deployment for one region shipped without `quota.ENTERPRISE`. The service started, passed its
health checks, and the first enterprise customer's upload failed with a `NullPointerException`
two days later. The on-call engineer wants that kind of mistake, and a mistyped tier in a key, to
stop the startup with a message that says what to fix.

`load(...)` in `src/main/java/practice/Quotas.java` already reads the file. Finish its two
`TODO`s:

- `tier(...)`: the tier a `quota.` key names;
- `checkComplete(...)`: the check that runs before `load` returns.

When you're done, a file that misses a tier, has no quotas at all, or names a tier that doesn't
exist fails the load with an `IllegalStateException` that names the problem. About eight lines in
total.
