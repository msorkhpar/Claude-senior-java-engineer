Checkout computes VAT from tables that the tax service publishes. Fetching them takes seconds and
the tax service bills every call, so the shop fetches them the first time a checkout needs them
and keeps them. At peak, a few hundred thousand checkouts a second ask for the tables, from many
threads. Last quarter's version fetched the tables twice during the morning rush, and the version
before it made every checkout wait in line for the tables.

Finish `src/main/java/practice/TaxTables.java`: the body of `rates()`, and the declaration of the
`rates` field it keeps the tables in (there is a `TODO` on each). Use the `lock` field the class
already has.

When you're done:

- the tax service is called on the first call to `rates()` and never again, even when two
  checkouts make the first call at the same moment;
- once the tables are fetched, `rates()` returns them without waiting, even while some other
  thread holds the lock;
- a checkout on any thread that gets the tables gets them as the tax service built them.

About ten lines in total. Some of the tests hold the tax service or the lock on one thread and
call `rates()` on another; one of them also reads how the `rates` field is declared.
