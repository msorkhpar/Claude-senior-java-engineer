The support team writes message templates such as `Hello {customer}, ticket {ticket} is closed.`
and the help desk fills them with each customer's data. The team picks the names, and some have
dots in them, such as `{invoice.no}`. Since refunds were added, some messages fail to send, and
one went out with a file path that had lost its backslashes.

Finish the two `TODO`s in `src/main/java/practice/MessageTemplate.java`:

- `render(values)`: the template with every value of the map filled in;
- `fill(text, name, value)`: `text` with `value` in place of `{name}`.

When you're done, every value appears exactly as the customer's data holds it, wherever its name
appears, and no other text changes. About four lines in total.
