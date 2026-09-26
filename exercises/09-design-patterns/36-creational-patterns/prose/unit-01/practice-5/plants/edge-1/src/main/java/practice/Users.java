package practice;

import java.util.Objects;
import java.util.Optional;

public final class Users {

    private Users() {
    }

    public interface UserRepository {
        /** Returns the user's name, or null when the id is unknown. */
        String findById(String id);
    }

    public static final class UserService {

        private static UserRepository repository;

        public UserService(UserRepository repository) {
            UserService.repository = Objects.requireNonNull(repository, "repository");
        }

        public Optional<String> findUser(String id) {
            return Optional.ofNullable(repository.findById(id));
        }
    }
}
