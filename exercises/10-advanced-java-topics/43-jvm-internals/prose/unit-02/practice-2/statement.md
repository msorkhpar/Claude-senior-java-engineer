The team's chat-ops bot reads a command from each message and runs it. Its `CommandTable` turns
the typed name into the command's position, which `reply` switches on. The table files commands
by `hashCode()` when it is built, and the admin page that lists those groups has to keep working,
so `byHash` stays.

Finish the two `TODO`s in `src/main/java/practice/CommandTable.java`:

- `file(command)`: put a command into `byHash` while the table is built;
- `indexOf(name)`: the typed name's position in the command list, or `-1` when it is no command.

When you're done, every command the table was built with is found at its own position, any other
name is unknown, and a message with no command text (`indexOf(null)`) is unknown too, not an
error. About twelve lines in total.

The tests build their tables from their own command names, and type each name as a new `String`.
