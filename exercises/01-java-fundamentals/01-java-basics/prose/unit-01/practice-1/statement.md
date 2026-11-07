The storage service places every new tenant: it gives the tenant a quota in bytes and picks the
shard that holds each of the tenant's keys. Two reports came in after the last onboarding wave.
A tenant with 4,000 users was given a negative quota and could store nothing. And one key of one
tenant, out of millions, fails on every write with an `ArrayIndexOutOfBoundsException` from the
shard router.

`TenantStorage.place` already puts the two answers together. Finish the two `TODO`s in
`src/main/java/practice/TenantStorage.java`:

- `quotaBytes(users, mbPerUser)`: the exact quota for any tenant the service accepts, up to
  tens of thousands of users with up to a hundred gigabytes each;
- `shardFor(key)`: a shard from `0` to `shards - 1` for every key, the same one each time.

About two lines in total.
