A clinic's front desk keeps one `Waitlist`. Several screens show it: the lobby screen sorts the
names alphabetically and hides some of them for privacy; each nurse's screen takes the next few
patients and adds walk-ins by hand. The desk reports two problems. After the lobby screen
refreshes, patients are called in alphabetical order instead of the order they arrived, and some
are missing from the queue. And a nurse's screen sometimes crashes when a new patient checks in at
the desk, while on a quiet morning it crashes as soon as the nurse adds a walk-in.

`Waitlist` already tracks who checks in and who is called in. Finish the two `TODO`s in
`src/main/java/practice/Waitlist.java`:

- `waiting()`: the list a screen gets of everyone waiting, in arrival order;
- `next(count)`: the list a nurse's screen gets of the first `count` patients.

When you're done, a screen can sort, trim and add to whatever list it got without changing the
queue, and its list stays as it was when it took it. About three lines in total.
