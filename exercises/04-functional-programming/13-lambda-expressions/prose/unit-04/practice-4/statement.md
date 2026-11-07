The operations console opens a dashboard per team, and each open dashboard forwards its team's
alerts (`"billing: 12 failed captures"`) to the team's chat channel. By design, the forwarding
keeps going after a dashboard is closed: the team still wants its alerts, and the alert feed has
no way to remove a listener. A heap dump after a day of use shows every dashboard ever opened
still in memory, 32 MB each.

Write `alertForwarder()` in `src/main/java/practice/Dashboard.java`: the listener that sends each
alert about the dashboard's team to its channel.

When you're done, a team's alerts reach its channel and nothing else's, and a closed dashboard can
be garbage-collected while its alerts keep arriving. About five lines.
