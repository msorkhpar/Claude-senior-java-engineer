The integration host runs partner plugins, and every plugin's calls to the partner API must come
out of one budget, `PluginHost.Quota.INSTANCE`. Each plugin gets a class loader of its own, and
that loader is meant to look in the plugin's jar first, so a plugin runs with the library versions
it bundles. Plugins are compiled against the host API (the nested types of `PluginHost`), and
their build copies that API into the jar.

The partner reports that the host goes over its budget whenever more than one plugin is
installed. Finish the two `TODO`s in `src/main/java/practice/PluginHost.java`, in the plugin
loader:

- `loadClass(name, resolve)`: which loader defines each class a plugin asks for;
- `sharedWithHost(name)`: which classes a plugin must get from the host, whatever its jar holds.

When you're done, every plugin draws on the host's one quota, and two plugins never share the
static state of their own classes. About twelve lines in total.

The tests build each plugin from a directory that holds both the plugin classes and the host's
classes, as a plugin jar does.
