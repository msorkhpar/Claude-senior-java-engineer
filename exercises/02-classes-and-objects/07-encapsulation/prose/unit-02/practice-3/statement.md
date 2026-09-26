Every getter returns by value, the page says: for a primitive the value is copied,
and for an object the **reference** is copied, so the caller holds the very object
the field points to. The page's `Example.getBirthDate()` therefore returns
`new Date(birthDate.getTime())`, a defensive copy. A field that must never change
gets a getter and no setter.

`java.util.Date` is mutable (`setTime` changes it). Write `Membership`:

- `Membership(String memberId, Date since)`;
- `String getMemberId()`: the id is fixed for life: no setter, and a `final` field;
- `Date getSince()`: the start date.

Nothing a caller does to a `Date` it passed in or got back may change the
membership.

Examples:

```
Date d = new Date(1_000L);
Membership m = new Membership("M-7", d);
m.getSince().setTime(0L);
m.getSince().getTime()      -> 1000
d.setTime(5L);
m.getSince().getTime()      -> 1000
```
