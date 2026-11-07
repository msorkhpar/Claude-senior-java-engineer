Release 4.2 of the invoicing module went out with three complaints:

- a partner ran `javap -v` on the released jar and read one of the team's review notes, word for
  word, in `InvoiceService`'s class file;
- the coverage report suddenly counts `InvoiceMapper`, which the mapper generator writes and the
  coverage tool is supposed to leave out (the tool reads compiled `.class` files, never sources);
- the cache layer caches nothing.

All three annotations are declared in `src/main/java/practice/Invoicing.java`, each with a `TODO`
above the retention it shipped with. Give each the retention its reader needs, then write
`cacheSeconds(Class<?>)`, which the cache layer calls on every service class at start-up: each
method the class declares with `@Cached`, by name, with its seconds.

About eight lines in total. The tests read the compiled class files the same way the coverage
tool does, and call `cacheSeconds` on classes of their own.
