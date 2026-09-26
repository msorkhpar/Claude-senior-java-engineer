package practice;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import practice.Users.UserRepository;
import practice.Users.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsersTest {

    private static UserRepository knowing(Map<String, String> users) {
        return users::get;
    }

    @Test
    void findsAUserThroughTheInjectedRepository() {
        UserService service = new UserService(knowing(Map.of("42", "ada")));

        assertThat(service.findUser("42")).isEqualTo(Optional.of("ada"));
    }

    @Test
    void twoServicesKeepTheirOwnRepositories() {
        UserService first = new UserService(knowing(Map.of("42", "ada")));
        UserService second = new UserService(knowing(Map.of("42", "grace")));

        assertThat(first.findUser("42")).isEqualTo(Optional.of("ada"));
        assertThat(second.findUser("42")).isEqualTo(Optional.of("grace"));
    }

    @Test
    void aMissingUserIsEmpty() {
        UserService service = new UserService(knowing(Map.of("42", "ada")));

        assertThat(service.findUser("7")).isEmpty();
    }

    @Test
    void aNullRepositoryIsRefusedAtOnce() {
        assertThatThrownBy(() -> new UserService(null)).isInstanceOf(NullPointerException.class);
    }
}
