The pricing page shows a few dozen stock quotes and asks for them with one `quoteAll(...)` call.
The remote quote service answers a whole list in one request. Last week a `Metered` decorator
went in front of it, so the dashboard can show how many symbols are quoted, and since then the
quote service's request count has jumped and its rate limit trips at lunchtime.

Finish `Metered.quoteAll(...)` in `src/main/java/practice/QuoteMetrics.java`: a page must still
cost the remote service one request, and the dashboard must count every symbol quoted, whether
it was asked for alone or as part of a page. About two lines in total.
