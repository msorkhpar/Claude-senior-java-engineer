The helpdesk's ticket list takes its order from the request: `GET /tickets?sort=opened&dir=desc`.
The API documents three sort keys, `priority`, `opened` and `title`, and two directions, `asc` and
`desc`. The `ticket` table has more columns than that, among them `customer_tier`, which agents
must never be able to rank tickets by.

`TicketList.titles(sort, dir)` already builds and runs the query. Write `orderBy(sort, dir)` in
`src/main/java/practice/TicketList.java`: the text that goes after `ORDER BY`. A documented request
sorts the list; any other request is refused with an `IllegalArgumentException`, and nothing it
contains reaches the database. The key `opened` sorts by the column `opened_at`.

About eight lines in total. The tests run against an H2 in-memory database.
