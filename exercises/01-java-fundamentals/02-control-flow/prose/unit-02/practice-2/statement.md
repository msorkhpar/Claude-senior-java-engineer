The billing service downloads a paginated invoice report by running one task per page on a
thread pool, then putting the pages back in order. Pages are numbered from 1.

Finish the `TODO` in `src/main/java/practice/PageFetcher.java`: `tasks(pageCount)` builds the
tasks that `fetchAll` hands to the pool. The pool runs them after `tasks` has returned, in any
order.

When you're done, a report of four pages comes back as pages 1, 2, 3 and 4, each fetched once.
About four lines in total.
