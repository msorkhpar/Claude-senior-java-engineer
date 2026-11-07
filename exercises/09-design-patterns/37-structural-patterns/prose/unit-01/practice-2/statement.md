Partners send the stock service a nightly feed, one `name;quantity` record per line, over a
connection that delivers the bytes in whatever pieces the network produces. Each partner declares
the charset of its feed in its contract: most use UTF-8, two older ones Latin-1. A record with a
broken name must not reach the stock tables: last month a partner switched to Latin-1 without
saying so, and a week of town names was stored with `�` in them before anyone noticed.

Finish `forEachLine` in `src/main/java/practice/FeedLines.java`. It hands each line of the feed to
the consumer, in order, decoded in the charset passed in, and returns how many lines there were.
Bytes that aren't valid in that charset end the read with an `IOException`. Leave the stream open:
the caller closes it. About five lines in total.
