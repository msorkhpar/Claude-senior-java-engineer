An `EnumMap` is an array indexed by ordinal: no hashing, O(1) access, and
iteration in declaration order. The page's real-world example keeps an access
log keyed by permission, with an entry put in for **every** permission up
front.

Write `static Map<Permission, List<String>> whoHas(Map<String, Set<Permission>> grants)`
for the nested `Permission { READ, WRITE, EXECUTE, DELETE, ADMIN }`. `grants`
maps each user name to the permissions that user holds. The result maps each
permission to the users holding it:

- every permission has an entry, with an empty list when nobody holds it;
- the permissions iterate in declaration order;
- each list of users is sorted by name.

| `grants` | `whoHas(grants)` |
|---|---|
| `carol: [READ, ADMIN]`, `alice: [READ, WRITE]`, `bob: [READ]` | `{READ=[alice, bob, carol], WRITE=[alice], EXECUTE=[], DELETE=[], ADMIN=[carol]}` |
| empty | `{READ=[], WRITE=[], EXECUTE=[], DELETE=[], ADMIN=[]}` |
