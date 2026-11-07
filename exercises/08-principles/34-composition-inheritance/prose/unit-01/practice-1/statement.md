The config service keeps feature flags in a `Map<String, Boolean>`, and the audit needs to know
how many flag writes each deploy made. Flags are written one at a time from the admin UI (`put`)
and in batches by the config sync (`putAll`). The store isn't always the same map: production
passes a `HashMap`, the nightly report a `TreeMap` so its output is sorted. The audit also
compares each snapshot of the flags with the previous one, and keeps the snapshots it has
already filed in a `HashSet` so the same one isn't filed twice.

`WriteCountingMap.java` holds a `WriteCountingMap` that wraps whatever map it is given, on top of
a `ForwardingMap` that already passes every `Map` method on to that map. Finish the four `TODO`
methods, about six lines in total:

- `WriteCountingMap.put` and `putAll`: `writes()` counts every mapping written through the
  wrapper, an overwrite of an existing key included, whichever map is underneath;
- `ForwardingMap.equals` and `hashCode`: a wrapper compares and hashes like the map it wraps, so
  the audit's comparisons and its `HashSet` treat it as the flags it holds.
