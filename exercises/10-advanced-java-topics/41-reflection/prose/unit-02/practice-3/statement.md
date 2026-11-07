The warehouse's ops console lets an operator type a command, such as `reserve SKU-7731 12`, and
runs the public method of that name on a handler object. The console colours its reply by
status, and the on-call rota is paged for every 500. Operators complain that a typo in a quantity
pages someone, while a quantity typed as a word gets an answer that blames the command.

`call(...)` already finds the method. Finish the `TODO` in
`src/main/java/practice/Dispatcher.java`:

- `run(method, args)`: call the method on the handler and turn what happened into a `Reply`.

The statuses the console expects:

| What happened | Reply |
|---|---|
| the command returned | 200, `String.valueOf` of the result |
| the command threw `IllegalArgumentException` | 422, the command's message |
| the arguments don't fit the command's parameters | 400, starting with `bad arguments` |
| the command threw anything else | 500, the simple class name of what it threw |

About ten lines in total.
