The API gateway reads its rate limits from a YAML file: for each route, a list of requests allowed
per minute, one per customer tier. Last week someone wrote a limit as `"20"`, in quotes. The
gateway started without a complaint, and the first request to that route killed the request
scheduler with a `ClassCastException` in code that has no cast. On another night, a hot-reload
job edited the parsed tree after it was loaded, and the running limits changed under the
scheduler.

`RateLimits` already loads the file. Finish the `TODO` in
`src/main/java/practice/RateLimits.java`:

- `integers(route, node)`: turn one route's parsed node into the list of limits the rest of the
  gateway reads.

When you're done, bad configuration stops the load itself, with an `IllegalArgumentException`
whose message names the route, and the limits a `RateLimits` holds can't change after `load`
returns. The method needs no `@SuppressWarnings`. About ten lines in total.
