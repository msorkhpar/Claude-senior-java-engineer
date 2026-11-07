The invoicing run e-mails each invoice to the account's billing address. Last night it stopped
halfway with nothing in the log but `java.util.NoSuchElementException: No value present`, and the
on-call engineer spent an hour finding out which of 30,000 accounts caused it.

Write `invoiceAddress(accountId)` in `src/main/java/practice/BillingContacts.java`. An account
without a billing e-mail is normal: its invoices go to `RECEIVABLES`. An invoice for an account
that doesn't exist is a bug upstream, and the run should fail with an `UnknownAccountException`
that says which account it was. `find` and `Account.billingEmail()` already return `Optional`.
About four lines.
