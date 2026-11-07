Partner messages wait in an outbox until they are sent, and each has a deadline after which it is
no longer worth sending. Every few seconds the sender calls `dropExpired(now)` before it sends the
rest. The batch sender keeps its outbox in an `ArrayList`; the live sender, whose list other
threads read while it sends, keeps it in a `CopyOnWriteArrayList`. Both go through the same
`Outbox` class, and its callers keep their own reference to the list, so the expired messages must
leave that very list.

Finish `dropExpired(long now)` in `src/main/java/practice/Outbox.java`: drop every message whose
deadline is at or before `now`, keep the others in order, and return how many were dropped. It has
to work on both senders' lists. About three lines.
