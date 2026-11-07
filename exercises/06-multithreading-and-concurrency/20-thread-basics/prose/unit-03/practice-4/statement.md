The edge proxies stream their access logs to a collector, and the collector runs one
`LogTailer` per proxy. Each tailer reads its proxy's socket on a thread of its own. When a proxy
is retired, the collector calls `stop()` on its tailer, and that call never returns: the
collector's admin thread hangs until someone restarts it.

Reading already works. Finish the two `TODO`s in `src/main/java/practice/LogTailer.java`:

- `stop()`: end the tailer and wait for its thread;
- `readFailed(...)`: what happens when a read throws.

When you're done, `stop()` returns even when the proxy has gone quiet, retiring a proxy doesn't
page on-call, and a connection that really breaks still does. About six lines in total.

The tests hand the tailer a stream that behaves like a socket's on a platform thread.
