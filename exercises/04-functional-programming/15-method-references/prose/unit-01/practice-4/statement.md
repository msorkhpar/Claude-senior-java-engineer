When a referenced method is overloaded, the compiler picks the overload by the
**target type**: the parameter types of the functional interface stand in for the
argument types of an ordinary call.

`List<Integer>` has two `remove` methods: `remove(int index)` and `remove(Object o)`.
`String` has many `valueOf` methods, among them `valueOf(char[])` and `valueOf(Object)`.
Write three factories in `Overloads`, each returning a method reference:

1. `BiConsumer<List<Integer>, Integer> removeValue()` removes the given **value**.
2. `ObjIntConsumer<List<Integer>> removeAt()` removes the element at the given **index**.
3. `Function<char[], String> text()` turns a character array into the string it spells.

| list before | call | list after |
|---|---|---|
| `[10, 20, 30]` | `removeAt().accept(list, 1)` | `[10, 30]` |
| `[10, 2, 30, 40]` | `removeValue().accept(list, 2)` | `[10, 30, 40]` |

and `text().apply(new char[] {'a', 'b', 'c'})` is `"abc"`.

The same method name can mean different things here; which one runs depends on the
interface you return it as.
