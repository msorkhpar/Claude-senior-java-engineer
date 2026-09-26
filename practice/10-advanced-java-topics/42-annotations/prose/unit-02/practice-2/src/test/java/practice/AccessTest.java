package practice;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTest {

    static class UserService {
        @Access.Role("ADMIN")
        @Access.Role("MANAGER")
        public String listUsers() {
            return "user list";
        }

        @Access.Role("ADMIN")
        public String createUser() {
            return "user created";
        }

        public String health() {
            return "ok";
        }
    }

    private static Method method(String name) throws Exception {
        return UserService.class.getDeclaredMethod(name);
    }

    private static String runtime(String s) {
        return new StringBuilder(s).toString();
    }

    @Test
    void readsRepeatedRolesAndGuardsTheCall() throws Exception {
        Method listUsers = method("listUsers");

        assertThat(Access.rolesOf(listUsers)).containsExactly("ADMIN", "MANAGER");
        assertThat(Access.canCall(listUsers, Set.of(runtime("MANAGER")))).isTrue();
        assertThat(Access.canCall(listUsers, Set.of("GUEST"))).isFalse();
    }

    @Test
    void aSingleRoleIsNotWrapped() throws Exception {
        Method createUser = method("createUser");

        assertThat(createUser.getAnnotation(Access.Roles.class)).isNull();
        assertThat(Access.rolesOf(createUser)).containsExactly("ADMIN");
    }

    @Test
    void aMethodWithoutRolesIsOpen() throws Exception {
        Method health = method("health");

        assertThat(Access.rolesOf(health)).isEmpty();
        assertThat(Access.canCall(health, Set.of())).isTrue();
    }
}
