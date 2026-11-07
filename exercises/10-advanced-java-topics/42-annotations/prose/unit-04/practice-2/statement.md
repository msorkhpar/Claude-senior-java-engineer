The HTTP server reads its route table from `practice.generated.Routes`, which
`RouteIndexProcessor` writes at compile time from every class marked `@Route("/path")`. The team
has just added a client generator to the build: another annotation processor that writes handler
classes of its own, also marked `@Route`. Since then the route table has been the problem.

Finish `record(...)` and `timeToWrite(...)` in `src/main/java/practice/RouteIndexProcessor.java`.
Both are called on every call of `process`. The table must list every handler of the build, the
ones in the sources and the ones other processors generate, and writing it must never fail the
build. About four lines in total.

The tests compile handler classes with `javax.tools.JavaCompiler`, your processor, and a stand-in
for the client generator.
