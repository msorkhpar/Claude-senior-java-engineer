A `non-sealed` permitted subclass opens the hierarchy at one chosen point.
`Commands` uses that to give a closed command set one extension point:

- `sealed interface Command permits Quit, Echo, Extension`;
- `record Quit()` and `record Echo(String text)`;
- `abstract non-sealed class Extension` with two abstract methods,
  `String name()` and `String run()`. Anyone may extend it; the library ships
  one, `Help`.

Write `static String execute(Command c)` with a `switch` over the command:

- `Quit` -> `"bye"`;
- `Echo` -> its text;
- any `Extension` -> `"[" + name() + "] " + run()`.

The library cannot list every extension, because it does not know them: the
switch handles them all through the methods `Extension` declares.

**Examples**

- `execute(new Quit())` -> `"bye"`
- `execute(new Echo("hi"))` -> `"hi"`
- `execute(new Help())` -> `"[help] commands: quit, echo"`
