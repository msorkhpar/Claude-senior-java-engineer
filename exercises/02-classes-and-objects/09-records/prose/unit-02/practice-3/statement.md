A record's generated `equals`, `hashCode` and `toString` treat each component
with that component's own methods. For an **array** those are `Object`'s:
identity equality and a `toString` like `[D@1b6d3586`. An array is also
mutable, so a record holding one is only shallowly immutable.

Complete `record Reading(String sensor, double[] values)` so that it behaves
like a value:

- `equals`: an equal `sensor` (compared by its text, not by which `String`
  object it is) and equal array **contents** (`Arrays.equals`);
- `hashCode`: consistent with that `equals` (`Arrays.hashCode`);
- `toString`: `Reading[sensor=<sensor>, values=<Arrays.toString(values)>]`;
- the compact constructor keeps a **copy** of the array it is given, and the
  `values()` accessor returns a **copy** too, so nobody outside can change a
  reading.

## Examples

```
new Reading("s1", new double[] {1.5, 2.0}).equals(new Reading("s1", new double[] {1.5, 2.0}))  -> true
new Reading("s1", new double[] {1.5, 2.0}).toString()   -> "Reading[sensor=s1, values=[1.5, 2.0]]"

double[] raw = {1.5, 2.0};
Reading r = new Reading("s1", raw);
raw[0] = 99;                  -> r.values()[0] is still 1.5
r.values()[1] = 99;           -> r.values()[1] is still 2.0
```
