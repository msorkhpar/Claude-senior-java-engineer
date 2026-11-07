Every service sends its operational alerts through one channel, chosen in its config under
`alerts.channel`: `email`, `sms`, `chat`, or whatever else the build provides. Services that don't
set the key send by email. Last week a service was deployed with a misspelled channel name, and
its disk-full alert was the first anyone heard of it, after the disk had filled.

Write `choose` in `src/main/java/practice/AlertService.java`. The constructor calls it once, and
`raise` sends every alert through the channel it returns. A misspelled name must be found when
the service is deployed, by an `IllegalArgumentException` from the constructor, and must never
send an alert anywhere else. About five lines in total.
