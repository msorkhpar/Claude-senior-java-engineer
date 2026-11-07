The payments ledger keeps its entries in hash sets and looks them up with `contains`. A refund
of 2.50 CHF and a 2.50 CHF card surcharge are different entries, but reconciliation keeps losing
one of them, and only on some days: it depends on which of the two was recorded first.

`Fee` extends `Amount` and adds a reason. Write both `equals` methods in
`src/main/java/practice/Ledger.java`, so that:

- two amounts are equal when their cents and currency are;
- two fees are equal when their cents, currency and reason are;
- an amount and a fee are never equal, whichever of the two is asked.

`hashCode` is already written. About six lines in total.
