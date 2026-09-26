A state machine is the page's first use case: the states are enum constants,
each state declares its own valid transitions, and `transitionTo` enforces
them. The page's pitfall is a `moveTo(next)` that just returns `next`.

Fill in each constant's `validTransitions()` from this table, and write the
three shared methods:

| state | may move to |
|---|---|
| `CREATED` | `PENDING_PAYMENT`, `CANCELLED` |
| `PENDING_PAYMENT` | `PAID`, `CANCELLED` |
| `PAID` | `PROCESSING`, `REFUNDED` |
| `PROCESSING` | `SHIPPED`, `CANCELLED` |
| `SHIPPED` | `DELIVERED`, `RETURNED` |
| `DELIVERED` | `RETURNED` |
| `RETURNED` | `REFUNDED` |
| `CANCELLED`, `REFUNDED` | nothing (terminal) |

- `boolean canTransitionTo(OrderState target)`: whether `target` is one of this
  state's valid transitions.
- `OrderState transitionTo(OrderState target)`: returns `target` when the move
  is valid, and throws `IllegalStateException` otherwise.
- `boolean isTerminal()`: whether the state has no valid transitions.

The enum's states are shared by every order in the program, so a caller that
changes the set `validTransitions()` returned must not change what the state
allows.

| call | answer |
|---|---|
| `CREATED.transitionTo(PENDING_PAYMENT)` | `PENDING_PAYMENT` |
| `CREATED.transitionTo(DELIVERED)` | throws `IllegalStateException` |
| `CANCELLED.isTerminal()` | `true` |
| `DELIVERED.isTerminal()` | `false` |
