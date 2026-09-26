`"hello".equals("Hello")` is `false`. `equalsIgnoreCase` compares without regard to case.
Calling it on the literal you know, `"quit".equalsIgnoreCase(input)`, is also safe when
`input` is `null`: the answer is simply `false`.

Write `isQuit(String input)` in `Commands`. It says whether the user typed the command
`quit`, in any case, with any spaces around it:

- `isQuit("quit")`, `isQuit("QUIT")` and `isQuit("Quit")` are `true`;
- `isQuit("  quit ")` is `true`;
- `isQuit("quite")` and `isQuit("")` are `false`, and so is `isQuit(null)`.
