The page's Q1 gives one annotation per retention policy:

- `@ReviewNeeded(reason)`: a note for people and the compiler. It must
  **never reach the class file**: SOURCE.
- `@GeneratedCode(generator)`: for bytecode tools. It must be **kept in the
  class file but hidden from reflection**: CLASS, which the page says to
  state explicitly even though it is the default.
- `@Cacheable(cacheName, ttlSeconds)`: read by a framework while the program
  runs: RUNTIME.

`Retained` declares the three without any `@Retention`, plus the page's
`Service` that uses them. Give each the right `@Retention`, then write
`cached(Class<?> type)`: one line `"<method> -> <cacheName> (<ttlSeconds>s)"`
per method `type` declares with `@Cacheable`, sorted.

The tests also read `Service`'s compiled `.class` file to see which
annotations the compiler wrote into it.

| check | expected |
|---|---|
| `cached(Service.class)` | `[config -> default (300s), getUsers -> users (600s)]` |
| `@ReviewNeeded` in `Service`'s class file | absent |
| `@GeneratedCode` in `Service`'s class file | present |
| `Service.class.isAnnotationPresent(GeneratedCode.class)` | `false` |
