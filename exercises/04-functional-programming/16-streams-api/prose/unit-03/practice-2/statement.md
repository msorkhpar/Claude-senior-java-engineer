The fraud team reviews large payments as they come in. `PaymentWatch` reads the live payment feed
and puts the next few payments of at least a set amount on the reviewers' sheet, each scored by
the risk service, highest score first. Two problems reach you at once: the risk service's quota is
used up by mid-morning, and since the service switched from hourly batch files to the live feed,
the review sheet never loads.

`PaymentWatch.reviewSheet(...)` already formats the sheet. Finish the `TODO` in
`src/main/java/practice/PaymentWatch.java`: `nextForReview(...)` returns the next `slots` payments
at or above `reviewFromCents`, in the order they arrive, each assessed once, sorted by score from
highest to lowest. Only payments that go on the sheet are sent to the risk service.

The feed is a `Stream` that doesn't end while the service runs. About five lines.
