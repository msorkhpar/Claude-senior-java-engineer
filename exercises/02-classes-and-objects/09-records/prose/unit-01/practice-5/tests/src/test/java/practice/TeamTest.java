package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamTest {

    @Test
    void aTeamKeepsItsMembers() {
        Team team = new Team("Core", List.of("Ann", "Ben"));
        assertThat(team.name()).isEqualTo("Core");
        assertThat(team.members()).containsExactly("Ann", "Ben");
        assertThat(team.withMember("Eve").members()).contains("Eve");
    }

    @Test
    void changingTheCallersListLaterChangesNothing() {
        List<String> names = new ArrayList<>(List.of("Ann", "Ben"));
        Team team = new Team("Core", names);
        names.add("Cid");
        assertThat(team.members()).containsExactly("Ann", "Ben");
    }

    @Test
    void theMembersCannotBeChangedThroughTheAccessor() {
        Team team = new Team("Core", new ArrayList<>(List.of("Ann", "Ben")));
        assertThatThrownBy(() -> team.members().add("Dee"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(team.members()).containsExactly("Ann", "Ben");
    }

    @Test
    void withMemberLeavesTheOriginalAlone() {
        Team team = new Team("Core", List.of("Ann", "Ben"));
        Team bigger = team.withMember("Eve");
        assertThat(bigger).isNotSameAs(team);
        assertThat(bigger.name()).isEqualTo("Core");
        assertThat(bigger.members()).containsExactly("Ann", "Ben", "Eve");
        assertThat(team.members()).containsExactly("Ann", "Ben");
    }
}
