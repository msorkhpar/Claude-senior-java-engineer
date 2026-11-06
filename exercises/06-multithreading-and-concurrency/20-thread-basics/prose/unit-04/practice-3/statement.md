The search service starts an `IndexWorker` at boot and sends it the first search terms as soon
as `start()` returns. The worker can't index anything before it has loaded its synonym table.
Usually that takes a few milliseconds, but when the configuration is being reloaded at the same
moment, the loader has to wait for the configuration's lock, and on a cold cache it computes for
seconds. On those boots the first terms meet a worker with no table.

`IndexWorker` already starts its thread, loads the table on it and indexes what it is sent.
Finish the two `TODO`s in `src/main/java/practice/IndexWorker.java`:

- `signalReady()`: called on the worker's thread once its table is loaded;
- `awaitReady()`: called by `start()`, returns once the worker is ready to index.

When you're done, `start()` returns once the table is loaded, however long the loader takes and
whatever it waits for. About four lines in total, a field included.

The tests hold the loader back, once behind a lock and once in a long computation, and check
whether the table was loaded at the moment `start()` returned.
