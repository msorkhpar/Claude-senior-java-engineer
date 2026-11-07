The billing service gets exchange rates from a remote service through `Retrying.Rates`. The
connection drops a few times an hour, so the client is wrapped in a proxy from `Retrying.wrap`
that is meant to try a failed call again. The billing code catches `IOException` around every
rate lookup and falls back to yesterday's rate; anything else fails the invoice run.

`Retrying.wrap(...)` already builds the proxy. Finish the `TODO` in
`src/main/java/practice/Retrying.java`: `call(...)`, which makes one call on the proxy happen.

When you're done, a network failure (`IOException` or a subclass) is tried again, up to the
number of attempts the proxy was built with. If the last attempt fails too, the billing code gets
the rate service's own last exception, so its `catch (IOException e)` still runs. Any other
failure reaches the billing code as itself, after one call. About six lines in total.
