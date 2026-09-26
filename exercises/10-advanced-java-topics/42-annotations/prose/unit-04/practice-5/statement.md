A compile-time processor extends `AbstractProcessor`, declares what it
handles with `@SupportedAnnotationTypes`, and runs inside `javac`. It
inspects elements through `javax.lang.model`, generates new files through
the `Filer`, and reports problems through the `Messager`: a problem reported
as an **ERROR fails the compilation**, with the message on the element.

`FieldNamesProcessor.FieldNames` (given, `SOURCE` retention) marks a class.
Write `FieldNamesProcessor.process(...)`: for each class marked
`@FieldNames`, generate a class `<Name>Fields` **in the marked class's own
package** with

```java
public static final java.util.List<String> NAMES = java.util.List.of(...);
```

listing the class's **instance** fields (not its `static` ones) in
declaration order. `@FieldNames` on anything that is not a class is reported
as an error on that element whose message contains `@FieldNames`.

| source | generated |
|---|---|
| `@FieldNames class Point { int x; int y; String label; }` | `PointFields.NAMES` = `[x, y, label]` |
| `package demo.geo; @FieldNames class Place { static int COUNT; String name; double lat; }` | `demo.geo.PlaceFields.NAMES` = `[name, lat]` |
| `@FieldNames interface Shape { }` | compilation fails with an error on `Shape` |

The tests hand your processor to `javac` themselves, so no
`META-INF/services` file is needed.
