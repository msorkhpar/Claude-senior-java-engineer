The shop keeps each member's loyalty points in a `PointsAccount`. Purchases add points from the
request threads, refunds take them away, and batch jobs run promotions ("double every balance
this weekend") and expiry ("half of it expires") over every account, at the same time as the
shop is busy. Support has a pile of tickets from members whose points went back to an old
balance after a promotion, and the balance field is already `volatile`.

Finish `apply(...)` in `src/main/java/practice/PointsAccount.java`. A rule computes the new balance
from the current one and has no side effects; some rules take a while to compute. When you're
done, no rule is lost when rules arrive together, and each rule applies to the balance as it is
when the rule takes effect. You may change how the balance is stored. About three lines in total.

The tests hold one rule in the middle of its computation while another thread applies a second
rule, then let the first one finish.
