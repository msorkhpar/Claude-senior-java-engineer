An `EnumSet` is a bit vector: one bit per constant, so `contains`, `add` and
`retainAll` are single bitwise operations, and it iterates in declaration
order. The page builds a permission system on it, and names two pitfalls:

- An empty `EnumSet` comes from `EnumSet.noneOf(Permission.class)`.
  `EnumSet.of()` needs at least one element, and `EnumSet.copyOf` of an empty
  plain collection throws, because it cannot tell the enum type.
- `public static final Set<Permission> ALL = EnumSet.allOf(...)` hands every
  caller a mutable set. Wrap shared sets so nobody can change them.

Complete `Permission { READ, WRITE, EXECUTE, DELETE, ADMIN }`:

- The four shared sets: `READ_ONLY` = {READ}, `READ_WRITE` = {READ, WRITE},
  `FULL_ACCESS` = every permission, `NO_ACCESS` = none. Callers cannot change
  them.
- `static Set<Permission> combine(Set<Permission>... sets)`: the union of the
  sets; no sets, or only empty ones, give an empty set.
- `static Set<Permission> intersect(Set<Permission>... sets)`: the
  permissions every set holds; no sets at all give an empty set.

| call | answer |
|---|---|
| `combine(READ_ONLY, Set.of(EXECUTE))` | `[READ, EXECUTE]` |
| `intersect(FULL_ACCESS, READ_WRITE, Set.of(WRITE, ADMIN))` | `[WRITE]` |
| `combine(NO_ACCESS, Set.of())` | `[]` |
| `intersect()` | `[]` |
| `FULL_ACCESS.remove(ADMIN)` | throws `UnsupportedOperationException` |
