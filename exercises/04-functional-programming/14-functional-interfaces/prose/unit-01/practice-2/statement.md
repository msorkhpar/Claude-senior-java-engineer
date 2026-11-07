Devices upload event ids in batches, and `EventIngest` takes each batch. The raw archive must
hold every id the devices sent, repeats included: it is what support reads when a device
misbehaves. Subscribers, on the other hand, must hear about each event once, the first time its
id arrives, whether the repeat comes in the same batch or a later one.

The previous version did both jobs in a stream and returned the batch size from it. The count was
always right; the archive had holes, and subscribers were paged twice for the same door opening.

Write `ingest(batch)` in `src/main/java/practice/EventIngest.java`. It archives every id,
announces the new ones, remembers what it has seen, and returns how many ids the batch held.
About four lines in total.
