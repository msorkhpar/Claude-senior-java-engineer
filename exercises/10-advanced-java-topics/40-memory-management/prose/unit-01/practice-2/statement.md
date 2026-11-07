The file service virus-scans every upload before storing it. Uploads wait in an `UploadQueue`
that lives as long as the service; the scanner takes them one at a time, and an operator can
discard everything still waiting. Each upload carries its whole body. After a busy morning the
queue is empty, yet the heap stays near its limit, and the service later fails with
`OutOfMemoryError: Java heap space` in the PDF thumbnailer. A heap dump taken after the morning
shows the morning's upload bodies, all reachable from `UploadQueue.items`.

`put(...)`, `size()` and the growth of the array are done. Finish the two `TODO`s in
`src/main/java/practice/UploadQueue.java`:

- `take()`: hand out the oldest waiting upload, or `null` when none is waiting;
- `clear()`: discard every waiting upload.

When you're done, the queue keeps an upload in memory only while it is waiting, whether it left
the queue by being scanned or by being discarded. About eight lines in total.

The tests ask the garbage collector for a full collection and check, with weak references, which
upload bodies are still in memory.
