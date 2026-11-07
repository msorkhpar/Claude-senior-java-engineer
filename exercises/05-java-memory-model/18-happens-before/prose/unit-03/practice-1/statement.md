The trading desk's alerting service runs one price watcher per rule: "tell me when NRDX goes
above 250.00", "tell me when KLMT drops below 40.00". The desk writes its own watchers by extending
`PriceWatcher`, builds all of them from its configuration at startup, and starts them once the alert
channel is up. Since the market-data feed began handing every new subscriber the last price, each
restart during trading hours pages the desk with a burst of alerts like `NRDX at 23175 is above 0`,
and some rules don't alert on the opening trade at all.

Fix `src/main/java/practice/PriceWatchers.java`. `start()` is still a `TODO`, and the rest of
`PriceWatcher` is yours to change too; keep the public constructors and methods as they are, because
the desk's own watchers use them.

When you're done, a watcher hears nothing until it is started; once started it hears the last price
at once and every trade after it, and judges each one against its own limit, whichever class
defines that limit. About four lines in total.

The tests use a feed that hands the last price to a new subscriber on the subscribing thread, and a
watcher of the desk's own that the file doesn't know about.
