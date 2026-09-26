The page rewrites a stream pipeline from lambdas to method references so that it reads
like a description: `filter(Employee::active)`, `Comparator.comparingDouble(Employee::salary)`,
`map(Employee::name)`, `groupingBy(Employee::department, ...)`.

`Employee` is a record `(String name, String department, double salary, boolean active)`.
Write two methods in `TopEarners` (the page writes every step that only calls a method
as a method reference):

1. `List<String> names(List<Employee> employees, int limit)` returns the names of the
   best-paid **active** employees, highest salary first, at most `limit` of them.
2. `Map<String, Double> averageSalaryByDepartment(List<Employee> employees)` returns each
   department's average salary over its **active** employees.

| employees | call | result |
|---|---|---|
| Ann (Eng, 90 000), Bo (Ops, 70 000), Cy (Eng, 110 000) — all active | `names(employees, 2)` | `["Cy", "Ann"]` |
| the same | `averageSalaryByDepartment(employees)` | `{Eng=100000.0, Ops=70000.0}` |

Only active employees count, in both methods.
