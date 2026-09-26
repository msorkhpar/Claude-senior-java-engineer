Data hiding starts with the fields: **make them private**, and expose only what is
necessary. The page's one exception is a constant (`public static final`), which
cannot be changed by anybody.

Write `FieldAudit.exposed(Class<?> type)`, a code-review helper. It returns the
names of the fields **declared by `type` itself** that break the rule, sorted
alphabetically. A field breaks the rule when:

- it is not `private`, and
- it is not a constant: a field that is both `static` and `final`.

Fields the compiler adds on its own (synthetic fields; see
`java.lang.reflect.Field.isSynthetic()`) are not the author's and are never
reported.

Examples:

```
class Account { public double balance; private String id; }
exposed(Account.class)   -> [balance]

class Limits { static final int MAX = 5; static int counter; int size; }
exposed(Limits.class)    -> [counter, size]
```
