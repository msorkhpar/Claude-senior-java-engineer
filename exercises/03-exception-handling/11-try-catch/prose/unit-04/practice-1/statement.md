An ingest run writes sensor rows into one file per partition of the object store. It opens a
partition's writer when the first row for that partition arrives, so the number of writers is
known only at run time and they can't be declared in one try-with-resources header. The run
itself is the resource: the job does `try (PartitionWriters run = ...)`.

The object store flushes a writer's last rows when it is closed, and that flush can fail. After a
night when the store was briefly unavailable, two things were found: partitions whose files were
never closed, and an alert that named one failed partition when three had failed.

Write `close()` in `src/main/java/practice/PartitionWriters.java` so the run ends the way it
would if every writer had been declared in a try-with-resources header in the order it was
opened. About ten lines.
