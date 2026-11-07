The depot's scanner workers record every parcel event in `ShipmentJournal`, many threads at once,
and a flusher copies the journal to disk. The journal is a `StringBuffer`, picked because it is
thread-safe, yet the night shift's file has lines such as
`parcel=NL-5531 depot=parcel=BE-0907 depot=ANR-2 grams=610`. Looking up an event's depot can take
a moment, because it asks another service.

Write `record(...)` in `src/main/java/practice/ShipmentJournal.java`. When you're done, every line
in the journal is exactly one event's line, however the threads overlap, and a thread waiting on
a slow depot lookup doesn't stop the other threads from recording. About four lines.

The tests make one event's lookup wait while another thread records, so the overlap happens on
every run.
