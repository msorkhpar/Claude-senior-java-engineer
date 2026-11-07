The payments service keeps accounts by a 64-bit id. Events reach it through a decoder that turns
each message into a `Map<String, Object>`, and old card terminals still check accounts with a
32-bit id of their own. Since the mobile app started sending events, support hears two
complaints: some events fail with a `ClassCastException`, and every old terminal reports
"account closed" for accounts that are open.

`ownerOf` and `open` already work. Finish the two `TODO`s in
`src/main/java/practice/AccountDirectory.java`:

- `accountId(event)`: the id in the event's `"accountId"` field, whichever type the decoder chose
  for it (see the class comment);
- `isOpen(terminalAccountId)`: whether the terminal's id names an open account.

About two lines in total.
