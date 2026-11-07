The shop's front page runs sale campaigns, and marketing moves them around the calendar. This
morning they tried to move the autumn sale from 11–15 October to 25–29 October, and the admin
screen refused it, although the new week is a perfectly good campaign.

`Campaign` keeps a first and a last day, both included, and a campaign never ends before it
starts. Its constructor, getters and `setStartsOn` are written. Finish the two `TODO`s in
`src/main/java/practice/Campaign.java`:

- `setEndsOn(...)`: change the last day of a running campaign;
- `moveTo(...)`: move the campaign to a new window.

When you're done, a campaign can be moved from any valid window to any other valid window, and a
date or a window the campaign refuses leaves it exactly as it was. About eight lines in total.
