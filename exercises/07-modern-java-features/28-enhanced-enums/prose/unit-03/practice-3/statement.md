When a bank rejects a transfer, it sends a four-character reason code such as `AM04`, and the
payments team works from the `Reason` it stands for. The table of reasons is edited by hand
whenever the bank publishes new codes, and the bank sometimes starts sending a code before the
table has it.

Finish the two `TODO`s in `src/main/java/practice/Rejections.java`:

- `index(items, codeOf)`: the code-to-item index that `Reason` builds once from its constants;
- `Reason.fromCode(code)`: the reason for a code.

When you're done, every code in the table finds its reason. A table in which two entries share a
code is refused with an `IllegalArgumentException` naming the code, rather than quietly keeping one
of them, and a code the table doesn't have yet comes back as unknown, so whoever handles the
rejection can tell it from a code the bank explained. About eight lines in total.
