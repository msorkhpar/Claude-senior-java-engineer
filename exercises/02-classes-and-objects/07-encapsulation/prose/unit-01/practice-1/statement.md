The card service sells prepaid gift cards. By law a card holds at most `GiftCard.CAP` cents, and
a card that has been cancelled (reported lost, say) takes no new money. `load` and `spend` already
enforce this.

Customers now want to move money from one of their cards to another. Write `transferTo(target,
cents)` in `src/main/java/practice/GiftCard.java`.

When you're done, money moved onto a card obeys the same rules as money loaded onto it, and a
transfer that either card refuses throws `GiftCard.RefusedException` and leaves both balances
exactly as they were. About three lines in total.
