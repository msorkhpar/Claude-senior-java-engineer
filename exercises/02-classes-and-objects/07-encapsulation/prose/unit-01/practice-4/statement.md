The page's interview answer builds a singleton from access modifiers: a **private
constructor**, a **private static** field holding the one instance, and a **public
static** method handing it out. It then warns that its lazy version is not
thread-safe: two threads can both see `instance == null` and both construct.

`Settings` loads its values in a private constructor that is given to you and takes
a moment to run. Keep that constructor as it is. Write:

- `public static Settings getInstance()`: always returns the one `Settings`
  object, **also when many threads call it for the first time at once**;
- `public String get(String key)`: returns the loaded value for `key`, or `null`.

`created()` (given) counts how many times the constructor has run.

Examples:

```
Settings.getInstance() == Settings.getInstance()   -> true
Settings.getInstance().get("mode")                  -> "prod"
Settings.created()                                  -> 1
```

The page names three thread-safe ways; any of them is accepted.
