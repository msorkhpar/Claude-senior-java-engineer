package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import practice.Grants.Access;
import practice.Grants.Role;

// Each test reads its own roles, so a solution that changes a role's grants is caught by the
// test about that role and cannot disturb the others.
class GrantsTest {

    @Test
    void eachRoleHasItsBaseGrants() {
        assertThat(Grants.base(Role.VIEWER)).containsExactly(Access.VIEW);
        assertThat(Grants.base(Role.OWNER)).containsExactlyInAnyOrder(Access.values());
    }

    @Test
    void extraGrantsAddToTheRole() {
        assertThat(Grants.effective(Role.MANAGER, EnumSet.of(Access.DELETE)))
                .containsExactlyInAnyOrder(Access.values());
    }

    @Test
    void storedValuesReadBack() {
        assertThat(Grants.decode("SHARE")).containsExactly(Access.SHARE);
        assertThat(Grants.decode("DELETE,COMMENT")).containsExactlyInAnyOrder(Access.COMMENT, Access.DELETE);
    }

    @Test
    void whatACallerDoesWithARolesGrantsDoesNotChangeTheRole() {
        Set<Access> handedOut = Grants.base(Role.COMMENTER);
        try {
            handedOut.add(Access.DELETE);
        } catch (UnsupportedOperationException refused) {
            // refusing the change is one right answer
        }
        assertThat(Grants.base(Role.COMMENTER)).containsExactlyInAnyOrder(Access.VIEW, Access.COMMENT);
    }

    @Test
    void oneUsersExtraGrantsDoNotReachTheRole() {
        Grants.effective(Role.EDITOR, EnumSet.of(Access.SHARE));
        assertThat(Grants.base(Role.EDITOR)).doesNotContain(Access.SHARE);
        assertThat(Grants.effective(Role.EDITOR, EnumSet.noneOf(Access.class))).doesNotContain(Access.SHARE);
    }

    @Test
    void aUserWithNoExtraGrantsReadsBackAsNone() {
        assertThat(Grants.decode("")).isEmpty();
    }
}
