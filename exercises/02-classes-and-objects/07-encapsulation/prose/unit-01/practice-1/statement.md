Java has four access levels. The page's own examples declare one member at each:

| example | level | keyword |
|---|---|---|
| `PublicExample.publicVariable` | public | `public` |
| `ProtectedExample.protectedVariable` | protected | `protected` |
| `DefaultExample.defaultVariable` | package-private | *(none)* |
| `PrivateExample.privateVariable` | private | `private` |

Write `AccessTable.canAccess(Level level, From from)`. It answers whether code in
the place `from` may use a member declared with `level`.

`From` names where the using code sits, relative to the class that declares the member:

- `SAME_CLASS`: inside the declaring class itself;
- `NESTED_CLASS`: inside a class nested in the same top-level class;
- `SAME_PACKAGE`: in another class of the same package;
- `SUBCLASS_OTHER_PACKAGE`: in a subclass that lives in a different package;
- `OTHER_PACKAGE`: in an unrelated class of a different package.

Examples:

```
canAccess(PUBLIC,  OTHER_PACKAGE)   -> true
canAccess(PRIVATE, SAME_PACKAGE)    -> false
canAccess(PRIVATE, SAME_CLASS)      -> true
```

Mind the two levels in the middle: read the page's definitions of protected and
package-private carefully.
