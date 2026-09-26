`Collectors.groupingBy(classifier)` groups a stream into a `Map<K, List<T>>`. A second argument, a *downstream*
collector, turns each group into something else: a count, an average, a list of names.
`Collectors.partitioningBy(predicate)` is the special case with the keys `true` and `false`.

`DeptReport.Employee` is a record `(String name, String dept, int salary)`. Write four static methods in
`DeptReport`:

1. `Map<String, Long> headcount(List<Employee> staff)`: the number of employees per department.
2. `Map<String, Double> averageSalary(List<Employee> staff)`: the average salary per department.
3. `Map<String, List<String>> namesByDept(List<Employee> staff)`: the employees' names per department, in list order.
4. `Map<Boolean, List<String>> splitByPay(List<Employee> staff, int threshold)`: under `true`, the names of those
   paid at least `threshold`; under `false`, the others; both in list order.

With the staff Alice (Engineering, 90000), Bob (Engineering, 85000), Carol (Marketing, 70000),
Dave (Marketing, 72000) and Eve (Engineering, 95000):

| call | answer |
|---|---|
| `headcount(staff)` | `{Engineering=3, Marketing=2}` |
| `averageSalary(staff)` | `{Engineering=90000.0, Marketing=71000.0}` |
| `namesByDept(staff)` | `{Engineering=[Alice, Bob, Eve], Marketing=[Carol, Dave]}` |
| `splitByPay(staff, 80000)` | `{false=[Carol, Dave], true=[Alice, Bob, Eve]}` |

An average need not be a whole number, a salary may be as large as an `int` allows, a threshold may put everyone
on one side, and two employees may share a name.
