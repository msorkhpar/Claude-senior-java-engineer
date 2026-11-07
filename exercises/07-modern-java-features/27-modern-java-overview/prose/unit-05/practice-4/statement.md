The warehouse sends order lines such as `sku=K-204;qty=12;wrap;note=ring twice`. Fields are
separated by `;`, and a field is `key=value`, except a flag such as `wrap`, which has no `=` and
whose value is the empty string.

`Fields.valueOf(line, key)` already walks the line one field at a time. Finish the two helpers it
uses, in `src/main/java/practice/Fields.java`:

- `fieldEnd(line, from)`: where the field that starts at `from` ends;
- `separatorIn(line, from, to)`: the position in `line` of that field's `=`, or -1 when the field
  is a flag.

Any field must be readable, the last one included, and a key the line doesn't have gives an empty
`Optional`. About three lines in total.
