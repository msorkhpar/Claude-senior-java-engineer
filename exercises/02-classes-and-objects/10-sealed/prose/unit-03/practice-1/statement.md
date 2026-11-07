The support desk refunds orders through `Refunds.refund(orderId, cents)`. Three steps can refuse:
the order store doesn't know the order, the amount is more than what is left to refund (or isn't
positive), or the bank refuses the payment. All three are everyday outcomes, and the desk's screen
shows the reason to the agent.

Two incidents last month: a refund larger than the order went to the bank anyway, and an agent saw
"refund failed" for an order id with a typo in it, with no hint what was wrong.

Finish `refund` in `src/main/java/practice/Refunds.java`. The order store and the gateway already
answer with a `Result`, and `refundable(order, cents)` checks the amount. When you're done, money
reaches the gateway only when the order exists and the amount is allowed, and every refusal comes
back as an `Err` that the desk can show as it is. About twelve lines in total.

The tests use their own orders, amounts and gateway answers.
