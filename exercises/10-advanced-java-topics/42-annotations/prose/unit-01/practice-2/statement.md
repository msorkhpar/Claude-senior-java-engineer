The warehouse service builds every shipment through `Shipment.of(...)`. The team turned on
`-Xlint:unchecked -Werror`, and now the routing module, which ships one parcel of any type from a
generic helper, no longer compiles: javac warns at its call to `Shipment.of`. A teammate's first
fix also went wrong in production: the loading screen reuses one array for every truck, and
shipments that had already left changed their parcels when the next truck was loaded.

Finish the `TODO` in `src/main/java/practice/Shipment.java`:

- `of(parcels)`: build the shipment from its parcels.

When you're done, generic code calls `of` without a warning, and a shipment's parcels stay what
they were when `of` returned. About three lines in total, the method's declaration included.

The tests compile a generic caller with the JDK's compiler, against your class.
