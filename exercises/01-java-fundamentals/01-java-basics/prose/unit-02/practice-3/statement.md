An operations team hunts memory leaks with a small tool that reads a heap dump: every object in
the heap, the objects each one's fields refer to, and the GC roots (static fields and the local
variables of live threads). Its report lists the objects the next collection would free. An
engineer reads it to find what is holding the rest, so a report that names a held object as
garbage, or a freed one as held, sends them after the wrong code.

`garbage(...)` already builds the report. Finish the `TODO` in
`src/main/java/practice/HeapDump.java`:

- `retained(heap, roots)`: the objects of the heap that a collection must keep.

About seven lines.
