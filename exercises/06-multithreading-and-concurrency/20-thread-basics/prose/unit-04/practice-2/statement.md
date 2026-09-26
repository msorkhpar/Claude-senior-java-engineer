The page's transition table, and its answer to "can a thread go directly from
`BLOCKED` to `WAITING`?": **no**, a thread must be `RUNNABLE` to call `wait()`,
`join()` or `park()`. But `WAITING` to `BLOCKED` **is** possible: a thread
notified in `Object.wait()` must re-enter the monitor, and while another thread
holds it, it is `BLOCKED`.

| from | to |
|---|---|
| `NEW` | `RUNNABLE` |
| `RUNNABLE` | `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED` |
| `BLOCKED` | `RUNNABLE` |
| `WAITING`, `TIMED_WAITING` | `RUNNABLE`, `BLOCKED` |
| `TERMINATED` | nothing |

Write `Lifecycle.isValid(List<Thread.State> states)`: the states one thread
entered, in order. It is valid when it is not empty, starts at `NEW`, and each
state follows the one before it by a transition in the table. A thread need not
have ended yet, so the list may stop anywhere.

| states | answer |
|---|---|
| `NEW, RUNNABLE, TIMED_WAITING, RUNNABLE, TERMINATED` | `true` |
| `NEW, TERMINATED` | `false` |
| `NEW, RUNNABLE, BLOCKED, WAITING` | `false` |
| `NEW, RUNNABLE, WAITING, BLOCKED, RUNNABLE` | `true` |
| `NEW, RUNNABLE, TERMINATED, RUNNABLE` | `false` |
