Your team reviews lock-free code by writing each suspicious interaction down as a tiny program:
a few threads, each a list of reads and writes of shared variables. `LitmusModel` lists every
combination of values those reads can end with. Before it models what hardware and compilers do,
it needs the baseline: the results the threads can produce when nothing is reordered, which is
what the Java memory model promises a program without data races.

`LitmusModel.State` already performs one operation at a time and keeps memory and registers.
Finish `explore(state, results)` in `src/main/java/practice/LitmusModel.java`: from `state`, add
the registers of every finished execution to `results`. About eight lines in total.

When you're done, a reviewer can ask whether two threads that each set their own flag and then
check the other's can ever both find the other's flag unset, and get an answer they can trust.
