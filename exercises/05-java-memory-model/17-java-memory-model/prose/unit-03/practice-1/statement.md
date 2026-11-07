The status page shows tonight's maintenance window, and the deploy gate refuses deploys inside it.
Operators move the window from the admin console while thousands of page views read it. A few
times a week a customer sends a screenshot of a window from 10:00 to 03:00, and once a cancelled
window came back as "until 11:00" with no start.

`MaintenanceSchedule` already reschedules correctly. Finish the two `TODO`s in
`src/main/java/practice/MaintenanceSchedule.java`:

- `current()`: the window as it stands, for the page and the gate;
- `cancel()`: remove the window.

When you're done, a page view always shows a window that an operator actually scheduled (or
`Window.NONE`), however it lines up with a reschedule, and a cancel never leaves part of a window
behind. About five lines in total.

The constructor's `midReschedule` hook runs in the middle of `reschedule`; the tests use it to
pause a reschedule halfway and make calls from other threads while it waits.
