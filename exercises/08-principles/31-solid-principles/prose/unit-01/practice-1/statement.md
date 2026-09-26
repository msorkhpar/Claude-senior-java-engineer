The page's pitfall is a class with more than one reason to change. This one stores users,
validates them **and** announces them, so a change to the notification text and a change
to how users are kept both land in the same class:

```java
public static class UserServiceViolation {
    private final Map<String, String> users = new ConcurrentHashMap<>();
    private final List<String> notifications = new CopyOnWriteArrayList<>();

    public void createUser(String id, String name) {
        // validates id and name, stores the user, then records "User created: " + name
    }
}
```

Split it in `Users` into three nested classes, one responsibility each:

- `UserRepository` keeps users, one per id. `save(id, name)` throws
  `IllegalArgumentException` when either is null or blank, and saving an id again replaces
  its name. `findById(id)` returns an `Optional`, and `count()` says how many users are
  kept.
- `NotificationService` sends notifications. `send(message)` rejects a null or blank
  message with `IllegalArgumentException`, and `sent()` lists what was sent, in order.
- `UserService` coordinates the use case. It is **composed through its constructor** from
  the repository and the notifier it is given, and rejects a null one with
  `Objects.requireNonNull`. `createUser(id, name)` saves the user and then sends
  `"User created: " + name`; `findUser(id)` asks the repository.

For example, after `createUser("u1", "Alice")` the service finds `"Alice"` under `"u1"`,
and the notifier has sent `"User created: Alice"`. A user that fails validation is
neither saved nor announced. (The tests cannot see which of save and send runs first when both
succeed, so that order is the page's advice rather than a graded step.)
