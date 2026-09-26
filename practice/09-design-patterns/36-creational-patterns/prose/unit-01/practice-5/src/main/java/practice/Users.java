package practice;

import java.util.Optional;

public final class Users {

    private Users() {
    }

    public interface UserRepository {
        /** Returns the user's name, or null when the id is unknown. */
        String findById(String id);
    }

    public static final class UserService {

        public UserService(UserRepository repository) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public Optional<String> findUser(String id) {
            throw new UnsupportedOperationException("write findUser");
        }
    }
}
