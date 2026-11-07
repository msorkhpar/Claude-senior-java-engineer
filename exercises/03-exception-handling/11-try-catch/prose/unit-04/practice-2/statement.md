The billing importer reads the first record of each meter archive from the archive store, over a
pool of a few connections. `ArchiveReader.Decoder` checks an archive's header when it is
constructed, and closing a decoder closes the connection under it, the way a stream decorator
closes the stream it wraps.

Two things went wrong last month. A partner uploaded a few hundred CSV exports instead of
archives, and from then on every import failed with `pool exhausted`. And on an ordinary day, two
imports were found reading over the same connection at once.

Finish the two `TODO`s in `src/main/java/practice/ArchiveReader.java`: `firstRecord(...)` and
`Lease.close()`. When you're done, a rejected file costs the pool nothing, and a connection is
never handed to two readers. About six lines in total.
