A repair shop keeps a wait list of numbered tickets, served in the order customers joined, and
puts holds on accounts with unpaid bills. The front desk has two complaints: when a customer
leaves, sometimes someone else's ticket disappears, and a released hold stays on the account.

Finish `cancel(int ticket)` and `release(int accountId)` in
`src/main/java/practice/WaitList.java`. `cancel` takes that customer's ticket off the list and
keeps everyone else in order; `release` lifts the hold on that account. About two lines in total.
