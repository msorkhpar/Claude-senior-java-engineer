The tracing agent decides per handler class how closely to trace it, from a `@Traced(level = ...)`
annotation. Teams put it where it suits them: on the handler class, on a base class their handlers
extend, or on a service interface such as `Ingest`, which a handler may implement directly, through
its base class, or through another interface (`interface BulkIngest extends Ingest`). After the
last release the agent's dashboards show no traces from about a third of the handlers, all of them
handlers that teams say are annotated.

Finish `levelOf(Class<?> handler)` and `fromInterfaces(Class<?>[] interfaces)` in
`src/main/java/practice/TraceLookup.java`. A handler is traced at the level of the `@Traced` on
its class or nearest superclass; failing that, at the level of the first traced interface found
from the class upward; with none anywhere, it isn't traced. About twelve lines in total.
