Text blocks became standard in Java 15. The page's pitfalls are all about
whitespace:

- The compiler strips the **common leading whitespace** of the content lines
  and the closing `"""` line, so the closing delimiter's position decides how
  much indentation is removed. A closing `"""` further left than the content
  keeps the difference as indentation.
- A closing `"""` on its **own line** ends the text with a newline; placing it
  right after the last content line does not.
- A `\` at the end of a line suppresses that line terminator, joining the line
  to the next **with nothing in between**; any space you want must be written.

Pair a text block with `formatted()` to fill in values. Write the class
`Queries` using text blocks:

- `select(String table)` returns these four lines, each ending with `"\n"`,
  with no leading spaces:

  ```
  SELECT id, name, email
  FROM <table>
  WHERE active = true
  ORDER BY name ASC
  ```

- `inline(String table)` returns the same query as **one** line, parts
  separated by single spaces, with no line terminator at all.

| call | answer |
|---|---|
| `select("users")` | `"SELECT id, name, email\nFROM users\nWHERE active = true\nORDER BY name ASC\n"` |
| `inline("orders")` | `"SELECT id, name, email FROM orders WHERE active = true ORDER BY name ASC"` |
