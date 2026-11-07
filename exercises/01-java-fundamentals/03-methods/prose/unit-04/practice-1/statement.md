The billing service reads events off a queue and books them. Since the last release every
payment and every refund ends up parked with the events nobody handles, and customers' balances
stop moving. The queue delivers each event as an `Object`, in the order it was published.

`EventRouter` already has a handler for each kind of event, and a refund is a kind of payment.
Finish `route(Object event)` in `src/main/java/practice/EventRouter.java` so that each event
reaches the `handle(...)` method written for its kind: payments are credited, refunds debited,
and anything else is parked. About five lines in total.
