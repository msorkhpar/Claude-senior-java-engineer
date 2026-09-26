`Comparator.comparing(Employee::department)` builds a comparator from a key-extracting
method reference; `thenComparing` adds a tie-breaker, and `comparingDouble` avoids
boxing a `double` key. `reversed()` reverses **the whole comparator it is called on**.

`Employee` is a record `(String name, String department, double salary)`. Write
`Roster.ranked(List<Employee> employees)`: it returns the names ordered by department
(A to Z), then salary (highest first), then name (A to Z).

| employees | result |
|---|---|
| Alice (Engineering, 95 000), Bob (Marketing, 75 000), Charlie (Engineering, 88 000), Diana (Marketing, 82 000), Eve (Engineering, 95 000) | `["Alice", "Eve", "Charlie", "Diana", "Bob"]` |

Build it from method references. Be careful which part of the chain you reverse, and
what happens when two people in a department earn the same.
