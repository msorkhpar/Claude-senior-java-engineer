A subject can **push** the data with each notification, or observers can
**pull** what they need from the subject when told something changed. The
page's property-change observer does both: listeners get the old and the new
value, and may also read the property.

`PropertyChangeListener<T>` (given) has `void onChange(T oldValue, T newValue)`.
Write `ObservableProperty<T>`:

- `new ObservableProperty<>(initial)` holds `initial` (which may be `null`).
- `setValue(v)` does nothing when `v` **equals** the current value (by
  `equals`, not by `==`). Otherwise it stores `v` and calls
  `onChange(old, v)` on every listener. **The new value is stored before any
  listener runs**, so a listener that pulls with `getValue()` sees it.
- **`null` is an ordinary value**: changing to or from `null` notifies, and
  never throws.
- `addListener(l)` (`null` throws `IllegalArgumentException`),
  `removeListener(l)` and `listenerCount()`.

| value now | `setValue(...)` | listeners get |
|---|---|---|
| `"red"` | `"blue"` | `onChange("red", "blue")` |
| `"blue"` | another `"blue"` object | nothing |
| `null` | `"x"` | `onChange(null, "x")` |
| `"x"` | `null` | `onChange("x", null)` |
