The report server runs exporters that customers write themselves. Each plugin arrives as a jar
built with everything it was compiled against: the server's plugin API and the libraries the
customer tested with, which are often older or newer than the server's own copies.

Two attempts have shipped so far. The first gave every plugin its own `URLClassLoader` with no
link to the server, and the server could no longer use a single plugin: every one failed with
`ClassCastException` on the line that hands it to the scheduler. The second made the server's
loader the parent, and a customer's ledger export began formatting numbers the way the server's
version of their formatting library does, not the way the version in their jar does.

`PluginLoader` already knows what the jar holds (`inBundle`), which classes are the server's API
(`isApi`), and how to define a class from the jar (`defineFromBundle`). Finish the `TODO` in
`src/main/java/practice/PluginLoader.java`: `loadClass(...)`, which decides what each class name the
plugin uses resolves to.

When you're done, the server can use every plugin through the API types it knows, each plugin runs
on the library versions in its own jar, and anything the jar doesn't carry, the JDK included, comes
from the server. About eight lines in total.

The tests build a plugin jar in memory from compiled classes, so its classes have the same names as
classes the server already has.
