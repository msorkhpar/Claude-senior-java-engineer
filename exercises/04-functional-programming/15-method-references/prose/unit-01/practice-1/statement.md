A method reference is shorthand for a lambda that does nothing but call one existing
method. The page's table pairs each reference with its lambda:

| method reference | equivalent lambda |
|---|---|
| `String::toUpperCase` | `s -> s.toUpperCase()` |
| `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| `ArrayList::new` | `() -> new ArrayList<>()` |
| `String::compareTo` | `(s1, s2) -> s1.compareTo(s2)` |
| `StringBuilder::new` | `s -> new StringBuilder(s)` |

Complete `RefTable` so that each factory method returns the function in its row,
written as a method reference:

- `Function<String, String> upper()`
- `Function<String, Integer> parse()`
- `Supplier<ArrayList<String>> newList()`
- `Comparator<String> compare()`
- `Function<String, StringBuilder> builder()`

| call | result |
|---|---|
| `upper().apply("hello")` | `"HELLO"` |
| `parse().apply("42")` | `42` |
| `newList().get()` | an empty `ArrayList` |
| `compare().compare("a", "a")` | `0` |
| `builder().apply("abc").toString()` | `"abc"` |

A reference must behave like its lambda on every call, not only the first, and a
reference to an instance method has to decide which argument is the receiver.
