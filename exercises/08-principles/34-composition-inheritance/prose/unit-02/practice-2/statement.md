A reporting service keeps one connection pool per tenant in `TenantPools`, and every request asks
for its tenant's pool. Opening a pool connects to the tenant's database and takes a while. The
database team reports that after each deploy some tenants get two or three pools' worth of
connections, and that once, after an admin reset a tenant's pool by hand, a pool the health check
had just opened was closed under running reports.

Finish the two `TODO`s in `src/main/java/practice/TenantPools.java`:

- `poolFor(tenant)`: the tenant's pool, opened with the `opener` the store was given when the
  tenant has none;
- `discard(tenant, pool)`: forget that pool, unless the tenant's pool is already a different one,
  and say whether it was discarded.

When you're done, every request for a tenant gets the same pool, a tenant's pool is opened once
however many requests arrive while it opens, and discarding a pool that was already replaced
leaves the replacement alone. About three lines in total.
