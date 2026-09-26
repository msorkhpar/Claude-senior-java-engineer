package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static practice.Permission.ADMIN;
import static practice.Permission.DELETE;
import static practice.Permission.EXECUTE;
import static practice.Permission.READ;
import static practice.Permission.WRITE;

/** Runs in name order, so the test that tries to change a shared set runs last. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class PermissionTest {

    @Test
    void combinesAndIntersectsPermissionSets() {
        assertThat(Permission.READ_ONLY).containsExactly(READ);
        assertThat(Permission.READ_WRITE).containsExactly(READ, WRITE);
        assertThat(Permission.FULL_ACCESS).containsExactly(READ, WRITE, EXECUTE, DELETE, ADMIN);
        assertThat(Permission.NO_ACCESS).isEmpty();
        assertThat(Permission.combine(Permission.READ_ONLY, Set.of(EXECUTE))).containsExactly(READ, EXECUTE);
        assertThat(Permission.intersect(Permission.FULL_ACCESS, Permission.READ_WRITE, Set.of(WRITE, ADMIN)))
                .containsExactly(WRITE);
    }

    @Test
    void combiningEmptySetsGivesNoAccess() {
        assertThat(Permission.combine(Permission.NO_ACCESS, Set.of())).isEmpty();
        assertThat(Permission.combine()).isEmpty();
    }

    @Test
    void intersectOfNothingIsEmpty() {
        assertThat(Permission.intersect()).isEmpty();
        assertThat(Permission.intersect(Set.of(), Permission.READ_ONLY)).isEmpty();
    }

    @Test
    void sharedSetsCannotBeChanged() {
        assertThatThrownBy(() -> Permission.FULL_ACCESS.remove(ADMIN)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> Permission.NO_ACCESS.add(ADMIN)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(Permission.FULL_ACCESS).contains(ADMIN);
        assertThat(Permission.NO_ACCESS).isEmpty();
        assertThatThrownBy(() -> Permission.READ_ONLY.add(ADMIN)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> Permission.READ_WRITE.add(ADMIN)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(Permission.READ_ONLY).containsExactly(READ);
    }
}
