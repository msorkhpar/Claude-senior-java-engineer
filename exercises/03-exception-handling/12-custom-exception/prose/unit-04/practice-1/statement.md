Checked exceptions must be caught or declared; unchecked ones need not be.
Which kind a type is follows from where it sits in the hierarchy. Write
`ExceptionKinds.isChecked(Class<? extends Throwable> type)`.

| type | `isChecked` |
|---|---|
| `IOException.class` | `true` |
| `SQLException.class` | `true` |
| `ClassNotFoundException.class` | `true` |
| `IllegalArgumentException.class` | `false` |
| `NullPointerException.class` | `false` |

The page names two branches of the hierarchy whose members are all unchecked,
and "member" means any descendant, however deep. `Class.isAssignableFrom`
answers "is this type that one or a subtype of it?".
