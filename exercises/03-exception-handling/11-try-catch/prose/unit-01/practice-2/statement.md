The checkout converts prices with rates from a primary provider, and asks a mirror when the
primary fails. Last Friday both failed for an hour, and the checkout's alert carried a stack trace
that named one provider's problem only. The team spent the hour on that one; the other provider's
problem, the one that needed a fix on our side, showed up only in the provider's own logs the next
day.

Finish the `TODO` in `fromMirror(...)` in `src/main/java/practice/RateClient.java`, which
`rate(...)` calls once the primary has failed. When the mirror fails too, the checkout gets a
`RatesUnavailableException` that names the pair, and its stack trace shows what went wrong with
each provider. About eight lines in total.

The tests check that each provider's own exception object can be reached from the exception the
checkout gets, through causes or suppressed exceptions: what a printed stack trace shows.
