The nightly export builds one task per tenant at 23:00 and hands the list to a scheduler, which
runs the tasks whenever it has capacity: sometimes in order, sometimes last task first. Each
export is stamped with its task number, and the morning report matches stamps against the plan.
Last week the report showed tenants under the wrong numbers, and one tenant that was added to the
admin screen at 23:05 got exported although it wasn't in the 23:00 plan.

`ExportPlan` already builds the list of tasks. Finish the `TODO` in
`src/main/java/practice/ExportPlan.java`:

- `task(tenants, index)`: the task for the tenant at `index`, numbered `index + 1`.

When you're done, every task exports the tenant and the number it was planned with, whatever
order the scheduler runs the tasks in and whatever the caller does to its list after `tasksFor`
returns. About three lines in total.
