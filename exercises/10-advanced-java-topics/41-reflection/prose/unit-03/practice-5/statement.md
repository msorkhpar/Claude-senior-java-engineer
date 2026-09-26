A DI container finds a class's constructor, **resolves each parameter type
recursively**, and calls `Constructor.newInstance` with the results. A
simple container recurses forever on a circular dependency (A needs B, B
needs A); guard it with a **"currently creating" set**.

Write `MiniContainer.resolve(type)` for the nested annotation
`MiniContainer.Inject` (given, for constructors):

- The constructor used is the one that **carries `@Inject`**; without one,
  the no-arg constructor. Any access level.
- Each parameter is resolved the same way, and each type is **created once**:
  later requests get the same instance.
- A type requested **while it is still being created** is a cycle:
  `IllegalStateException` whose message contains `"cycle"`.
- Other reflective failures become `IllegalStateException`.

| call | result |
|---|---|
| `resolve(Service.class)`, `@Inject Service(Repository)` | a Service; `process()` is `"Processed: data from DB"` |
| `resolve(Service.class)` again | the same Service |
| `resolve(Report.class)`, `Report()` and `@Inject Report(Repository)` | built by the `@Inject` one |
| `resolve(Ping.class)`, `Ping(Pong)` and `Pong(Ping)` | `IllegalStateException` ("cycle") |
