The export service writes each field of an order as a JSON value with `JsonValue.of(...)`. Two
kinds of caller use it: code that holds a field in a typed variable (`int quantity`,
`char grade`), and code that copies fields from a database row, which arrive as a
`Map<String, Object>`. The consumers of the export reject a file in which the same field is
written two different ways.

Finish `src/main/java/practice/JsonValue.java` so that:

- whole numbers, decimals and booleans are written unquoted, `12` and `true`, whichever kind of
  caller passes them;
- a single character is written as a one-character string, `"B"`;
- text is written quoted, and a missing row field as `null`.

You may add methods to the class. About eight lines in total.
