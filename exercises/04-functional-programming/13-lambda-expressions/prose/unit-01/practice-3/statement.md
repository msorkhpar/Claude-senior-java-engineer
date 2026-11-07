The admin console opens a `ConfigSession` for every signed-in user, and each open session is
told about every configuration change so its screens can refresh. The `ConfigWatcher` that
tells them lives as long as the server. After a week of uptime the server spends seconds on
every change, and a heap dump shows the watcher holding a listener for every session that was
ever opened.

Finish the two `TODO`s in `src/main/java/practice/ConfigSession.java`:

- `open()`: from now on, every changed key reaches this session's `onChange(...)`;
- `close()`: from now on, this session hears nothing, and the watcher holds nothing of it.

Other sessions on the same watcher are open at the same time, and closing one must not affect
them. You may add a field. About four lines in total.
