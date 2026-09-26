package practice;

import org.junit.jupiter.api.Test;

import practice.AccessIndex.Permission;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccessIndexTest {

    /** Grants in a fixed order that is not sorted by name. */
    private static Map<String, Set<Permission>> grants() {
        Map<String, Set<Permission>> grants = new LinkedHashMap<>();
        grants.put("carol", Set.of(Permission.READ, Permission.ADMIN));
        grants.put("alice", Set.of(Permission.READ, Permission.WRITE));
        grants.put("bob", Set.of(Permission.READ));
        return grants;
    }

    @Test
    void listsTheUsersOfEachPermission() {
        Map<Permission, List<String>> index = AccessIndex.whoHas(grants());
        assertThat(index.get(Permission.READ)).containsExactlyInAnyOrder("alice", "bob", "carol");
        assertThat(index.get(Permission.WRITE)).containsExactly("alice");
        assertThat(index.get(Permission.ADMIN)).containsExactly("carol");
    }

    @Test
    void aPermissionNobodyHoldsIsListed() {
        Map<Permission, List<String>> index = AccessIndex.whoHas(grants());
        assertThat(index).containsKey(Permission.EXECUTE);
        assertThat(index.get(Permission.EXECUTE)).isEmpty();
        assertThat(AccessIndex.whoHas(Map.of())).hasSize(5);
        index.get(Permission.EXECUTE).add("dave");
        assertThat(index.get(Permission.DELETE)).as("each permission has its own list").isEmpty();
    }

    @Test
    void permissionsComeInDeclarationOrder() {
        assertThat(AccessIndex.whoHas(grants()).keySet())
                .containsExactly(Permission.READ, Permission.WRITE, Permission.EXECUTE, Permission.DELETE, Permission.ADMIN);
    }

    @Test
    void usersAreSortedByName() {
        assertThat(AccessIndex.whoHas(grants()).get(Permission.READ)).containsExactly("alice", "bob", "carol");
    }
}
