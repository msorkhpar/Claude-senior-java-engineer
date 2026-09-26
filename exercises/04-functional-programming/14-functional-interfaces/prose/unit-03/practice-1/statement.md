`f.andThen(g)` runs `f` first and then `g`: it is `g(f(x))`. `f.compose(g)` runs `g` first and then `f`: it is
`f(g(x))`. Mixing the two up is a classic trap.

Write two methods in `Pipeline`. Each folds a list of `UnaryOperator<T>` steps into one `Function<T, T>`:

1. `inOrder(steps)` runs the steps first to last (build it with `andThen`).
2. `inReverse(steps)` runs them last to first (build it with `compose`).

| steps | input | `inOrder` | `inReverse` |
|---|---|---|---|
| `x -> x * 2`, `x -> x + 3` | `5` | `13` | `16` |
| `String::trim`, `s -> s + "!"` | `" hi "` | `"hi!"` | `"hi !"` |

A list may be empty. A step may return `null`; the step after it must not be handed that `null`.
