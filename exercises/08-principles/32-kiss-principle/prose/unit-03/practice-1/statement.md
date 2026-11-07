A bank's fraud monitor watches each customer's money from its own threads while transfers run.
Twice last month it froze an account because the customer's total had dropped by exactly the
amount of a transfer between their own checking and savings, and a minute later the total was
back. The monitor reads `total()` and `statement()`; the app calls `transfer(...)`.

Finish the three `TODO` methods in `src/main/java/practice/Wallet.java`. When you're done, a
transfer moves the amount from checking to savings, any number of threads can transfer at once
without losing a cent, and whatever the monitor reads, while any transfer is in progress, is
true of one moment: a total that counts the moving amount exactly once, and a statement with
both balances from before the transfer or both from after it.

`midTransfer` stays where it is, between the debit and the credit: the tests pause a transfer
there and read the wallet meanwhile. You may change the fields. About eight lines in total.
