`Comparator` is a functional interface with composition built in: `comparing`,
`thenComparingInt`, `thenComparing`, `reversed` and `nullsFirst`. A comparator that is used
again and again belongs in a `private static final` constant, so the lambda is built once.

`Person` is the record `Person(String name, int age, String department)`. Write three static
methods of `Roster`:

1. `Comparator<Person> byDepartmentAgeName()` orders by department, then by age, then by name,
   and returns the same comparator object every time.
2. `Comparator<Person> oldestFirst()` orders by age, oldest first.
3. `List<Person> sorted(List<Person> people)` returns a new list in `byDepartmentAgeName()`
   order, leaving `people` untouched; `null` entries go first.

| people | `sorted(people)` |
|---|---|
| `Charlie 30 HR`, `Alice 25 Engineering`, `Bob 25 Engineering`, `David 35 Engineering` | `Alice`, `Bob`, `David`, `Charlie` |

Mind the tie on both department and age, a `null` in the list, and how many comparators you
make.
