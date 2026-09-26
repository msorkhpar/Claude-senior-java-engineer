The page's violation gives each repository its own copy of the read-write locking:

```java
public String getUser(String id) {
    rwLock.readLock().lock();
    try {
        return users.get(id);
    } finally {
        rwLock.readLock().unlock();
    }
}
```

Its fix is one shared wrapper, `ReadWriteLockedResource<T>`: every repository uses it, so
moving from `ReentrantReadWriteLock` to another lock is a change in one class.
`UserRepository` and `ProductRepository` below already use it. Complete the wrapper:

- `read(readAction)` applies the action to the resource under the **read** lock and
  returns its result. **Readers do not block each other**: two reads run at the same time;
- `writeVoid(writeAction)` applies the action under the **write** lock, which **holds back
  every reader** until the write is done;
- each lock is released in a `finally` block;
- a `null` resource is refused by the constructor, and a `null` action by `read` or
  `writeVoid`, with `NullPointerException` **before any locking happens**.

Examples:

- a new `UserRepository` returns an empty list from `getAllUsers()`; after
  `addUser("u-1", "Ann")`, `getUser("u-1")` is `"Ann"`, and `getUser("u-9")` is `null`;
- two threads that each call `read` can both be inside their read actions at once;
- while a writer is inside `writeVoid`, a thread calling `read` waits, then sees the
  writer's change;
- `new ReadWriteLockedResource<>(null)` throws `NullPointerException`.
