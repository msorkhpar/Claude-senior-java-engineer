The canary analysis compares latency before and after a rollout. Every latency sample is stamped
with its offset in milliseconds from the moment the rollout started, so samples taken before it
have negative offsets, and they are counted in fixed windows held in a small ring of slots.

Three things are wrong on the dashboard: the old version's last slow samples show up in the new
version's first window, recording any sample from before the rollout throws
`ArrayIndexOutOfBoundsException`, and a window shows 0% slow unless every sample in it was slow.

`SampleWindows` already records and reports. Finish the three `TODO` methods in
`src/main/java/practice/SampleWindows.java`:

- `window(offsetMs)`: the window a sample falls in, as its comment defines it;
- `slot(window)`: the slot that holds a window's counts;
- `slowPercent(slow, total)`: the share of slow samples, as a whole percentage rounded down.

About three lines in total.
