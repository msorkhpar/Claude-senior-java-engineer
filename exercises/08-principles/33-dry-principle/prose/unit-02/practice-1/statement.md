Tenants of a search API pay for requests in quota units: a search costs 1, an export 5, and a bulk
request whatever the pricing service quotes for its rows. Until now each endpoint charged the
tenant itself, by reading `remaining(tenant)` and writing the new count back with `reset(...)`,
inside `synchronized (quota)`. The bulk endpoint, written last, did the same without the
`synchronized` block. Last month a tenant with 10,000 units made requests worth about 13,000.

Finish the `TODO`s in `src/main/java/practice/Quota.java`: `tryConsume(...)` and the three
endpoints of `Quota.Api`. When you're done, no charge is lost, whatever a concurrent search,
export, bulk request or refund does to the same tenant at the same moment, and a request the
tenant can't afford is refused without charging anything. About ten lines in total.

The tests stop one request partway, at a point of their choosing, and run another against the same
tenant before letting the first go on.
