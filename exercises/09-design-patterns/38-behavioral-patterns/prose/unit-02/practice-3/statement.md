A trading screen is made of widgets: charts, tickers, a blotter. Each one subscribes to a
symbol's prices on `PriceFeed` when the trader opens it, usually with a lambda or a method
reference, and closes what `subscribe` returned when the trader closes it. The blotter subscribes
the same listener to several symbols.

Finish `subscribe(String symbol, DoubleConsumer listener)` in
`src/main/java/practice/PriceFeed.java`. It registers the listener for that symbol and returns a
`Subscription`; closing that subscription stops exactly that registration, whatever else the same
listener or the same symbol is subscribed to, and lets the feed forget it. `tick` and the `Entry`
class are written. About three lines in total.
