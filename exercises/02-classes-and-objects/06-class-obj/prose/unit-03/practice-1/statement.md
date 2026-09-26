A **static** field belongs to the class: there is one copy, shared by every
object. An **instance** field belongs to one object: every object has its own. The
page's `InstanceCounter` uses a static field to count the objects created.

Write `Ticket`:

- `static int issued()` returns how many tickets have been created so far. It is
  called on the class, with no ticket needed.
- Every new `Ticket` raises that count by one and takes the count's new value as
  **its own** number, returned by `number()`: the first ticket is 1, the next 2,
  and so on.
- A ticket's number never changes afterwards.
- The page warns that static mutable fields are shared by every thread: tickets
  created by many threads at the same moment must all be counted, and no two
  tickets may get the same number. Guard the shared count with **the class's own
  lock**: a `static synchronized` method, or a `synchronized (Ticket.class)` block.
  While another thread holds that lock, creating a ticket waits.

**Examples**

```
Ticket.issued()   -> 0
a = new Ticket()  -> a.number() == 1
b = new Ticket()  -> b.number() == 2, a.number() == 1
Ticket.issued()   -> 2
```
