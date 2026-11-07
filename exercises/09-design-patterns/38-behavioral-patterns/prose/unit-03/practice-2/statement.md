The shop's back office sends refunds to the refund service over a link that sometimes loses a
reply: the service did the work, but the answer never came back. Finance found orders refunded
twice in the weeks the link was flaky. Support has the opposite complaint: a customer who
returned two identical items got one refund. And the back office's screen sometimes shows
"already processed" where agents expect a receipt number to give the customer.

Finish the two `TODO`s in `src/main/java/practice/Refunds.java`:

- `RefundService.handle(...)`: the service's side of one request;
- `RefundClient.refund(...)`: the back office's side, sending and retrying.

When you're done, a refund is paid once however many times the link had to ask, two refunds the
back office asked for separately are both paid, and the back office always gets the receipt of
the refund that was paid. About twelve lines in total.

The tests use a link that delivers every request and can drop the reply.
