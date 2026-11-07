The export service collects young garbage every few hundred milliseconds, and the team has a
Flight Recorder file from production to find out why. Nobody wants to read four thousand events
by hand. We want a small report: for one thread, how many bytes each of our own methods allocated,
so that the first line names code we can change.

`AllocationReport` already reads the recording, keeps the `jdk.ObjectAllocationSample` events of
the thread you ask for, and adds up a number per method. Finish the two `TODO`s in
`src/main/java/practice/AllocationReport.java`:

- `allocatingMethod(frames, appPackage)`: which of the application's methods a sample is charged
  to, as `"class.method"`;
- `bytes(sample)`: how many bytes the sample accounts for.

When you're done, the numbers in the report add up to roughly what the thread allocated, and every
entry is a method of the application that allocated, not one of the JDK's and not the method the
thread happened to start in. About six lines in total.

The tests make a real recording of a worker that builds CSV files and SVG badges, and compare the
report with the thread's own allocation counter. The `jdk.jfr.consumer` javadoc lists what an
event and a frame can tell you.
