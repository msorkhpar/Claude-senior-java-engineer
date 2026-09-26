The page's best practice: **don't call overridable methods from constructors**.
The superclass constructor runs first, before the subclass has set its own fields,
so a subclass override called from there sees those fields still `null` (or 0).

Write `Widget` and its nested static subclass `Widget.Button`:

`Widget(String id)`:

- `getId()` returns the id;
- `label()` returns `Widget`; subclasses override it;
- `summary()` returns `label() + "#" + id`.

`Button(String id, String text)` extends `Widget`:

- `label()` returns `Button:` followed by the current text;
- `setText(String text)` changes the text; the label and the summary follow it.

**Examples**

```
new Widget("w1").summary()                 -> "Widget#w1"
new Widget.Button("b1", "OK").summary()    -> "Button:OK#b1"
b.setText("Cancel"); b.summary()           -> "Button:Cancel#b1"
```
