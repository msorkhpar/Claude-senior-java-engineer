The support tool lists a customer's orders: the agent types the customer's e-mail address, may
pick a status, and can sort the result page by clicking a column header, which puts `?sort=total`
or similar into the URL. A penetration test got every customer's orders back by typing a quote
and a condition into the e-mail box, and another one appended its own SQL to the `sort`
parameter.

`OrderSearch.search(...)` builds a `Query`: the SQL text, which the caller passes to
`Connection.prepareStatement`, and the values the caller binds to it, in order. The text block and
the assembly around it are done. Finish the two `TODO`s in
`src/main/java/practice/OrderSearch.java`:

- `condition(column, value, params)`: the condition for one column and the value the agent typed;
- `orderBy(sortColumn)`: the column to sort by, `placed_at` when the URL has none. The page offers
  the columns in `SORTABLE`; anything else is refused with `IllegalArgumentException`.

When you're done, nothing an agent types or puts in the URL can change what the statement does.
About six lines in total. The tests read the `Query` you return; no database runs.
