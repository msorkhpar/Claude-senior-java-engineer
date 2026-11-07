The order service starts six integration connectors when it boots, and each connector's
constructor opens its own connection. Last Tuesday the customs partner's API was down for an
hour, and every restart in that hour failed: one connector that couldn't reach its partner kept
the other five from starting. The week before, a connector with a plain bug in its constructor
was skipped silently, and nobody noticed for two days.

`ConnectorFactory.createAll` already starts connectors and skips the ones that fail with a
`ConnectorException`. Finish the `TODO` in `src/main/java/practice/ConnectorFactory.java`:

- `create(type)`: a new connector made with the class's no-argument constructor.

When you're done, a connector whose constructor fails with a checked exception is skipped and
the others still start, the `ConnectorException` carries the constructor's own exception as its
cause, and an unchecked exception or an `Error` from a constructor reaches the caller exactly as
it was thrown. About ten lines in total.
