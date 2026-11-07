The orders endpoint pages its results, and a client may ask for its own page size with the
`pageSize` query parameter. Support keeps getting tickets that read "my page size is ignored",
and the warnings the endpoint records don't help: they should show what the client sent, and
they should only appear when the client sent something unusable.

`Pagination` already pages correctly once it knows the size. Finish `pageSizeFor(...)` in
`src/main/java/practice/Pagination.java`:

- an `Integer` from 1 to `MAX_PAGE_SIZE` is used as it is;
- with no `pageSize` at all, the endpoint's default is used and nothing is recorded;
- anything else gets the default too, and one warning that shows the value the client sent.

About five lines in total.
