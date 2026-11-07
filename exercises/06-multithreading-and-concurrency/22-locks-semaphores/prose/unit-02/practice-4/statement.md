The order store keeps every record on several replicas. A write is confirmed to the caller once a
quorum of replicas has acknowledged it; the rest catch up later. Replicas acknowledge from their
own threads, and a fast one may acknowledge before the writer has even started to wait.

Two reports came in after a replica failure last week. Writes that had exactly the quorum of
replicas behind them were reported as failed. And just after a slow replica went down, one write
was confirmed although only one replica had stored it.

Finish `acksForNextWrite()` and `awaitQuorum(...)` in `src/main/java/practice/QuorumWriter.java`.
`write(...)` already sends the record and passes each replica an acknowledgement that releases one
permit. When you're done, a write is confirmed exactly when a quorum of replicas acknowledged that
write, and a write that can't reach a quorum returns `false` once `timeoutMillis` has passed.
About two lines in total.
