The on-call tool pages engineers through `Alert`. Its scheduler keeps the current rotation in one
`String[]`, passes it to every alert it sends, and writes the next week's names into the same
array each Monday. Since the last release, three things have gone wrong: last week's alerts in the
history show this week's engineers, the rotation itself comes back in a different order after an
alert is sent, and on a fresh install, where no rotation has been set up yet, the first alert
crashed the scheduler.

`Alert` already renders a page. Finish the `TODO` in `src/main/java/practice/Alert.java`:

- `to(title, recipients...)`: the alert for those recipients, which the pager lists in
  alphabetical order.

When you're done, an alert lists the people it was created for, whatever the scheduler does to its
array afterwards, the scheduler's array is left as it was, and an alert sent before any rotation
exists goes to nobody. About five lines in total.
