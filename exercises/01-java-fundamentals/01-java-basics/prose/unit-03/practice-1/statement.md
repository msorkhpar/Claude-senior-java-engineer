The document service still writes into a legacy archive whose header stores the time a document
was saved as a signed 32-bit count of seconds since 1970. Lease contracts that run to 2040 have
started to show up in the archive as documents from 1903, and a scanned land deed from 1900 is
filed as if it were saved in 2036.

`ArchiveStamps.header` already builds the header line. Finish the `TODO` in
`src/main/java/practice/ArchiveStamps.java`: `toStampSeconds(epochMillis)` returns the seconds the
archive stores for that time, and refuses a time the format can't hold with an
`ArithmeticException`, so that no document is ever stamped with another date.

About three lines.
