The shop computes two deadlines from a date. Operations promises customers a ship date, and
support promises a reply date for every ticket. Both have called `WorkingDays.add` since someone
noticed the two loops were the same; it has since gained a `saturdays` parameter for the warehouse
and a `holidays` parameter for support.

This week brings one request from each team:

- **Operations:** the warehouse now works public holidays, as well as Monday to Saturday. An order
  ships at most two warehouse days after the day it was placed.
- **Support:** nothing changes about which days support works (Monday to Friday, never a public
  holiday), but a ticket opened on a day support doesn't work now counts as opened on support's
  next working day. The reply is due one support day after that.

Write `shipBy` and `replyBy` in `src/main/java/practice/Deadlines.java` so that each team gets its
request, and neither team's dates move because of the other's. About twelve lines in total.
