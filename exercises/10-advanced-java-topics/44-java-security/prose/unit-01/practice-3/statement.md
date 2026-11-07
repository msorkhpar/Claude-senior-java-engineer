The notification service sends mail through a relay described by `SmtpSettings`. It logs the
settings at start-up, and every delivery error carries the delivery job, a record that holds the
settings, so that the on-call engineer can see which relay and account failed. Those errors get
pasted into tickets, and a reviewer has noticed that a ticket shows the relay's password.

Write `toString()` in `src/main/java/practice/SmtpSettings.java`. The engineer still needs the host,
the port and the user, in the settings and in any record that holds them; nothing of the password
may show, not even its first few characters. About two lines in total.
