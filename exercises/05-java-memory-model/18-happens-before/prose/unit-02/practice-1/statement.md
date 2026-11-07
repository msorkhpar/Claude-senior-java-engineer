The account service records an audit event for every sensitive action. Each request thread has
an `AuditRecorder` that collects its events into batches and puts every full batch on an outbox
queue; a sender thread takes batches from the outbox and ships them to the archive. Since the
last release, which cut the recorder's allocations, the archive is missing most events, holds
some of them twice, and the sender logs `ConcurrentModificationException` every few seconds.

Finish `handOff()` in `src/main/java/practice/AuditRecorder.java`. When you're done, every
event a recorder records reaches the sender exactly once, in the batch it was recorded in, no
matter how far the recorder has got by the time the sender takes that batch. About two lines.

The tests record on one thread and take batches from the outbox on another.
