package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecuredProxyTest {

    public interface UserService {
        String getUser(int id);

        void deleteUser(int id);

        void archive(int id);

        void archive(String name);
    }

    public static class UserServiceImpl implements UserService {
        final List<String> done = new ArrayList<>();

        @Override
        public String getUser(int id) {
            if (id < 0) {
                throw new IllegalArgumentException("no user " + id);
            }
            return "User-" + id;
        }

        @Override
        @SecuredProxy.RequiresPermission("ADMIN")
        public void deleteUser(int id) {
            done.add("deleted " + id);
        }

        @Override
        public void archive(int id) {
            done.add("archived " + id);
        }

        @Override
        @SecuredProxy.RequiresPermission("ADMIN")
        public void archive(String name) {
            done.add("archived " + name);
        }
    }

    /** Permission names built at run time, so no two are the same String object by accident. */
    private static String name(String... parts) {
        return String.join("", parts);
    }

    @Test
    void checksThePermissionAnnotatedOnTheImplementation() {
        UserServiceImpl impl = new UserServiceImpl();
        UserService reader = SecuredProxy.secure(impl, UserService.class, Set.of(name("RE", "AD")));

        assertThat(reader.getUser(1)).isEqualTo("User-1");
        assertThatThrownBy(() -> reader.getUser(-1))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("no user -1");
        assertThatThrownBy(() -> reader.deleteUser(1))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Missing permission: ADMIN");
    }

    @Test
    void aRefusedCallNeverReachesTheTarget() {
        UserServiceImpl impl = new UserServiceImpl();
        UserService reader = SecuredProxy.secure(impl, UserService.class, Set.of(name("RE", "AD")));

        assertThatThrownBy(() -> reader.deleteUser(2)).isInstanceOf(SecurityException.class);

        assertThat(impl.done).isEmpty();
    }

    @Test
    void permissionNamesAreComparedByValue() {
        UserServiceImpl impl = new UserServiceImpl();
        UserService admin = SecuredProxy.secure(impl, UserService.class, Set.of(name("AD", "MIN")));

        admin.deleteUser(3);

        assertThat(impl.done).containsExactly("deleted 3");
    }

    @Test
    void overloadsAreToldApartByParameterTypes() {
        UserServiceImpl impl = new UserServiceImpl();
        UserService reader = SecuredProxy.secure(impl, UserService.class, Set.of(name("RE", "AD")));

        reader.archive(7);
        assertThatThrownBy(() -> reader.archive("old"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Missing permission: ADMIN");

        assertThat(impl.done).containsExactly("archived 7");
    }
}
