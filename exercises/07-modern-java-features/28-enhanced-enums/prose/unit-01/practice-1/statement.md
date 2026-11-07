The order service writes an audit line for every field of an order that changes:
`discount: Discount NONE -> VOUCHER`, or `total: type changed from Amount to Channel` when the
old and the new value are not of the same type. Since `LOYALTY` and `VOUCHER` got discount
rules of their own, the audit log has gone wrong for them: some lines name no type at all, and
a customer moving from no discount to a voucher is logged as a change of type.

Finish the two `TODO`s in `src/main/java/practice/AuditLog.java`:

- `typeName(value)`: the name the log shows for the value's type;
- `sameType(a, b)`: whether a change from `a` to `b` is an edit of one type.

When you're done, every constant of `Discount` is logged as a `Discount`, whichever constant it
is, and a constant of one enum is never the same type as a constant of another. About four lines
in total.
