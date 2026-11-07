The billing page has an "unpaid" panel. It is created when an accountant opens the page, and
every refresh asks it for the number of unpaid invoices, their total and their ids. The ledger
it is given is the billing service's live list, and new invoices are appended to it while the
page is open.

`UnpaidInvoices` already answers all three questions from one place. Finish the `TODO` in
`src/main/java/practice/UnpaidInvoices.java`: `unpaidOf(ledger)`, where those answers get the
unpaid invoices from.

When you're done, a panel answers every question on every refresh for as long as it is open, and
an invoice appended to the ledger after the panel was created shows up on the next refresh.
About two lines.
