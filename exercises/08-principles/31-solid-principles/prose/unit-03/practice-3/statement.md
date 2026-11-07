The SMS gateway asks a `Quota` before it sends a tenant's batch, and sends the part it is
granted. Internal senders get `Unlimited`; paying tenants are moving to a `DailyQuota` with a
budget of messages per day, and the gateway code that calls `grant` must not change for them.

Write `DailyQuota.grant` in `src/main/java/practice/Quotas.java`, keeping the contract written on
the `Quota` interface. The gateway's own `sendable` method and the tests call it with batches of
every size, including empty ones and ones bigger than what is left of the day. About three lines
in total.
