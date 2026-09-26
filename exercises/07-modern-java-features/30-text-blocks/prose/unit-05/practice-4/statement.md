Code generation is on the page's list of text block use cases, and its best
practices pair a text block template with `indent()` for the nested parts.

Write `JavaClass.source(String pkg, String name, Map<String, String> fields)`.
`fields` maps each field name to its type, in declaration order. Return the
source file, ending with a newline:

```text
package <pkg>;

public class <name> {

    private <type> <field>;
    ...

    public <name>() {
    }
}
```

- one `    private <type> <field>;` line per field, in the order the map gives;
- the field lines are followed by one empty line (no spaces on it);
- with no fields there is no field block at all, so the constructor comes one
  blank line after the class line.

| pkg, name, fields | the lines between `public class <name> {` and `    public <name>() {` |
|---|---|
| `com.example`, `User`, `{name=String, age=int}` | ``, `    private String name;`, `    private int age;`, `` |
| `com.example`, `Empty`, `{}` | `` |
