`enum Setting<T>` does not compile, so an enum cannot say that `MAX_RETRIES`
holds an `Integer` while `APP_NAME` holds a `String`. The page's preferred
workaround is a sealed generic interface whose permitted records play the
constants: `MaxRetries implements AppSetting<Integer>`,
`AppName implements AppSetting<String>`, and so on. Each "constant" then
carries its own type argument, and a switch over the sealed interface is
exhaustive.

The starter declares `AppSetting<T>` (with `key()`, `defaultValue()` and
`valueType()`) and its four records. Write:

- `static List<AppSetting<?>> values()`: one instance of each record, in the
  order `MaxRetries`, `AppName`, `DebugMode`, `TimeoutMs` (the sealed
  interface's stand-in for `Enum.values()`).
- `static Optional<AppSetting<?>> byKey(String key)`: the setting whose `key()`
  equals `key`, or empty.
- `static <T> T parse(AppSetting<T> setting, String raw)`: the setting's value
  read from `raw` (surrounding spaces ignored), in the setting's own type, so
  `Integer retries = parse(new MaxRetries(), "5")` needs no cast. A `null`
  `raw` gives the setting's `defaultValue()`. `DebugMode` accepts exactly
  `true` or `false`; any other text throws `IllegalArgumentException`.

| call | answer |
|---|---|
| `parse(new MaxRetries(), "5")` | `5` (an `Integer`) |
| `parse(new AppName(), " Shop ")` | `"Shop"` |
| `parse(new TimeoutMs(), "10000000000")` | `10000000000L` |
| `parse(new DebugMode(), "yes")` | throws `IllegalArgumentException` |
| `parse(new AppName(), null)` | `"DefaultApp"` |
| `byKey("timeout.ms")` | `Optional[TimeoutMs[]]` |

Keys arrive from configuration files, so the key passed to `byKey` is not the
same `String` object as the one a record returns.
