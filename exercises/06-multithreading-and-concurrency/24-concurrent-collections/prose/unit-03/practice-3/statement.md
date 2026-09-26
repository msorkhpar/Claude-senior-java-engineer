The page separates two kinds of change made while a `HashMap` is being walked.
Changing the **value** of an existing entry (`entry.setValue(...)`, or `put`
with a key already there) is not structural and is safe. **Adding or
removing** a key is structural, and the walk's next step throws
`ConcurrentModificationException`.

Write three methods that change a `Map<String, Integer>` of account balances
**in place** (the caller keeps using the same map object):

- `Ledger.addInterest(balances, percent)` adds `balance * percent / 100`
  (integer division) to every balance.
- `Ledger.dropBelow(balances, minimum)` removes every account whose balance is
  below `minimum`.
- `Ledger.prefixKeys(balances, prefix)` renames every account `k` to
  `prefix + k`, keeping its balance.

| balances | call | balances after |
|---|---|---|
| `{ann=1000, bob=2000}` | `addInterest(balances, 10)` | `{ann=1100, bob=2200}` |
| `{ann=1000, bob=150, cy=90, dee=5000, fay=500}` | `dropBelow(balances, 500)` | `{ann=1000, dee=5000, fay=500}` |
| `{ann=1000, bob=2000, cy=3000}` | `prefixKeys(balances, "old-")` | `{old-ann=1000, old-bob=2000, old-cy=3000}` |
