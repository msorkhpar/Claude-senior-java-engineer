The payment gateway hands every incoming payment to `PaymentScreen` before it is booked. Risk has
agreed these rules with finance (amounts in cents):

| Payment | Decision |
|---|---|
| a `Transfer` of up to 500,000 | `ACCEPT` |
| a `Transfer` above 500,000 | `REVIEW` |
| a `Transfer` above 5,000,000 | `HOLD` |
| a `Card` whose fraud score is below 60 | `ACCEPT` |
| a `Card` whose fraud score is 60 or more | `REVIEW` |
| a `Card` whose fraud score is 90 or more | `DECLINE` |
| a `Voucher` | `ACCEPT` |

Where two rows match a payment, the stricter one, lower in the table, wins.

The fraud score comes from the card network through `RiskScores`, and the network bills every
call. Last month's invoice was twice what finance expected for the number of card payments, and
nobody wants to repeat that.

Write `screen(...)` in `src/main/java/practice/PaymentScreen.java`. Decisions follow the table, a
card payment costs at most one score call, and any other payment costs none. About eight lines in
total.
