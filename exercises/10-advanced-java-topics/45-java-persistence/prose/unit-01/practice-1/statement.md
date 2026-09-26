The page's first pitfall builds SQL by pasting user input between quotes, so
the input `' OR '1'='1` rewrites the query. The fix is a `PreparedStatement`
whose `?` placeholders are filled with `setString` / `setBigDecimal`: **values
are bound, never concatenated into the SQL**.

The table (made by the tests) is
`employees(id INT PRIMARY KEY, name VARCHAR, department VARCHAR, salary DECIMAL(10,2))`.
Write `EmployeeSearch`, which is given an open `Connection`:

- `byName(name)`: the employees whose name equals `name`, ordered by id.
- `inDepartmentAbove(department, minSalary)`: the department's employees with
  `salary > minSalary` (strictly above), ordered by id. Money is a `BigDecimal`.

Bind every value: no value from the caller appears in the SQL text, not even
escaped. **The bound value is sent unchanged**: do not strip or
escape quotes yourself, so a name with an apostrophe still matches.

| rows | call | answer |
|---|---|---|
| Ada (Engineering, 90000.00), Linus (Engineering, 80000.00), Grace (Sales, 95000.00) | `inDepartmentAbove("Engineering", 80000.00)` | `[Ada]` |
| same | `byName("' OR '1'='1")` | `[]` |
| O'Brien (Sales, 70000.00) | `byName("O'Brien")` | `[O'Brien]` |
