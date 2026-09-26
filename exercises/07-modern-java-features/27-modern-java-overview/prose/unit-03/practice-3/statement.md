Java 15's hidden classes (JEP 371) are classes that cannot be discovered by
other classes: they are registered in no class loader's namespace, so
`Class.forName()` cannot find them, and their names contain a `/`. Since
Java 15 the JVM implements every lambda with a hidden class, and
`Class.isHidden()` tells whether a class is one. The page warns against assuming
hidden classes behave like normal classes, and interviewers like to ask how they
differ from anonymous classes.

Write the class `Classes`:

- `kind(Class<?> c)` returns `"hidden"` for a hidden class, `"anonymous"` for
  the class of an anonymous class expression (`new Runnable() { ... }`), and
  `"named"` for any other class. Note that a lambda's class is also marked
  *synthetic*; that does not make it anonymous.
- `loadable(Class<?> c)` returns whether `Class.forName(c.getName(), false, c.getClassLoader())`
  finds that same class. When the lookup fails, return `false` rather than
  throwing.

| call | answer |
|---|---|
| `kind(String.class)`, `loadable(String.class)` | `"named"`, `true` |
| `kind(((Runnable) () -> {}).getClass())` | `"hidden"` |
| `kind(new Runnable() { public void run() {} }.getClass())` | `"anonymous"` |
| `loadable(((Runnable) () -> {}).getClass())` | `false` |
| `loadable(int.class)` | `false` (no class is named `"int"`) |
