The notification service sends alerts to operators through one of several channels: SMS,
email, push. Each channel ships as a jar of its own, so a customer installs only the channels
they pay for, and the service never names a channel class in its code. A channel's jar holds a
class that implements `Channels.Notifier`, carries `@Channels.Channel("<name>")`, and is listed
in that jar's `META-INF/services/practice.Channels$Notifier`. The service's configuration names
the channel to use.

The service keeps its plug-in jars in a class loader of their own and hands it to
`Channels.forChannel(channel, plugins)`, the method you write in
`src/main/java/practice/Channels.java`. Last week's incidents decide what it must do:

- the service started on a thread whose context class loader can't see the plug-in jars, and
  it found no channel at all;
- a customer installed the push jar without its key; push's constructor throws, and the SMS
  alerts they had configured stopped too;
- a typo in the configuration (`"emial"`) sent the alerts somewhere other than where the
  customer asked, and nobody noticed for a week.

Return the configured channel's notifier, built once. A channel that no plug-in provides
fails with `IllegalArgumentException`, and its message names the channel. About seven lines.
