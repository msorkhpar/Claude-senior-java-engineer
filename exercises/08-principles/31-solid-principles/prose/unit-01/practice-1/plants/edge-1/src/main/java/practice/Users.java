package practice;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Users {

    private Users() {
    }

    /** Responsibility 1: keeping users. */
    public static final class UserRepository {
        private final Map<String, String> users = new ConcurrentHashMap<>();

        public void save(String id, String name) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("User ID cannot be null or blank");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("User name cannot be null or blank");
            }
            users.put(id, name);
        }

        public Optional<String> findById(String id) {
            return Optional.ofNullable(users.get(id));
        }

        public int count() {
            return users.size();
        }
    }

    /** Responsibility 2: sending notifications. */
    public static final class NotificationService {
        private final List<String> sent = new CopyOnWriteArrayList<>();

        public void send(String message) {
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Notification message cannot be null or blank");
            }
            sent.add(message);
        }

        public List<String> sent() {
            return List.copyOf(sent);
        }
    }

    /** Responsibility 3: coordinating the use case. */
    public static final class UserService {
        private final UserRepository repository;
        private final NotificationService notifications;

        public UserService(UserRepository repository, NotificationService notifications) {
            this.repository = Objects.requireNonNull(repository);
            this.notifications = Objects.requireNonNull(notifications);
        }

        public void createUser(String id, String name) {
            notifications.send("User created: " + name);
            repository.save(id, name);
        }

        public Optional<String> findUser(String id) {
            return repository.findById(id);
        }
    }
}
