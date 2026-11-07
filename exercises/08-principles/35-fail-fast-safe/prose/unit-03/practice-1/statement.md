The trading screen lets each user set price alerts: "tell me when NOVA goes above 182.50". The
market feed publishes every tick on its own thread, a few thousand a second, to the alerts held in
`AlertRegistry`; users add and remove alerts from the UI threads whenever they like, a few times
an hour each. A screen has a few dozen alerts at most.

The first version failed in production twice: once a tick was lost for half the alerts with an
exception in the feed thread, just as someone added an alert, and once adding an alert froze a
user's screen for four seconds while one alert was slow to send its e-mail.

Finish `src/main/java/practice/AlertRegistry.java`: decide how the alerts are held, and write
`subscribe`, `unsubscribe` and `publish`. When you're done, a tick reaches every alert that was
subscribed when the publish began, a publish never fails because of what the UI threads do, and
adding or removing an alert never waits for a publish. About eight lines in total.
