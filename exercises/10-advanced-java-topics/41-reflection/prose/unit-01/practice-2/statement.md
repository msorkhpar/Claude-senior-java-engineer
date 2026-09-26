`getFields()` and `getDeclaredFields()` differ in two ways, visibility and
inheritance:

| method | access levels | inherited? |
|---|---|---|
| `getDeclaredFields()` | all, private too | no |
| `getFields()` | public only | yes, from superclasses **and interfaces** |

Neither gives every field, private inherited ones included: for that you walk
up with `getSuperclass()`.

Write `FieldFinder`. Each method returns field **names**:

- `ownFields(type)`: the fields `type` itself declares, **every access
  level**, sorted.
- `publicFields(type)`: every public field, **inherited ones too**, including
  the **constants of interfaces** `type` implements, sorted.
- `allFields(type)`: every field of `type` and of **each superclass up to the
  top**, any access level. `type`'s own names first (sorted), then its
  parent's (sorted), and so on.

With `Parent { public publicField; private privateField; }` and
`Child extends Parent implements Tagged { public childPublic; private childPrivate; }`,
where `Tagged` declares `String TAG`:

| call | result |
|---|---|
| `ownFields(Child)` | `[childPrivate, childPublic]` |
| `publicFields(Child)` | `[TAG, childPublic, publicField]` |
| `allFields(Child)` | `[childPrivate, childPublic, privateField, publicField]` |
