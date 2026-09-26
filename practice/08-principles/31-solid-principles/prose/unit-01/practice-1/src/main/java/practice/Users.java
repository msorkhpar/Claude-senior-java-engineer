package practice;

import java.util.List;
import java.util.Optional;

public final class Users {

    private Users() {
    }

    /** Responsibility 1: keeping users. */
    public static final class UserRepository {

        public void save(String id, String name) {
            throw new UnsupportedOperationException("write save");
        }

        public Optional<String> findById(String id) {
            throw new UnsupportedOperationException("write findById");
        }

        public int count() {
            throw new UnsupportedOperationException("write count");
        }
    }

    /** Responsibility 2: sending notifications. */
    public static final class NotificationService {

        public void send(String message) {
            throw new UnsupportedOperationException("write send");
        }

        public List<String> sent() {
            throw new UnsupportedOperationException("write sent");
        }
    }

    /** Responsibility 3: coordinating the use case. */
    public static final class UserService {

        public UserService(UserRepository repository, NotificationService notifications) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public void createUser(String id, String name) {
            throw new UnsupportedOperationException("write createUser");
        }

        public Optional<String> findUser(String id) {
            throw new UnsupportedOperationException("write findUser");
        }
    }
}
