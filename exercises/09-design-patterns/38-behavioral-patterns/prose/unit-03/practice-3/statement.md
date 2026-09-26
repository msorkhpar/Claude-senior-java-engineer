A **macro** (composite) command groups commands into one: `execute()` runs
them first to last, and `undo()` must run their undos **in reverse order**.
The page's example shows why: after inserting `"Hello"` at 0 and `" World"`
at 5, removing `"Hello"` first would leave `" World"` too short for the
second undo.

`TextEditor`, `Command` and `InsertCommand` are given. Write
`MacroCommand(name, commands)`:

- `execute()` executes the commands in list order; **`undo()` undoes them
  from last to first**.
- The macro **keeps its own copy** of the list: changing the caller's list
  later changes nothing.
- **An empty or `null` list, or a blank name** (`null`, empty or only
  spaces), throws `IllegalArgumentException`.
- `description()` is `"Macro[<name>]: <n> commands"`; `commandCount()` is
  `n`.

| macro | call | editor |
|---|---|---|
| `hello-world`: `Insert(0, "Hello")`, `Insert(5, " World")` | `execute()` | `"Hello World"` |
| same | `undo()` | `""` |
| `MacroCommand("empty", [])` | build | `IllegalArgumentException` |
