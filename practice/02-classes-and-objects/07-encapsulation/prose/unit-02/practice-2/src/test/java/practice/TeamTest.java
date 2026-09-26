package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamTest {

    private static List<String> start() {
        return new ArrayList<>(List.of("Reading", "Swimming"));
    }

    @Test
    void listsTheMembersInOrder() {
        Team team = new Team("Club", start());
        team.addMember("Cooking");
        assertThat(team.getName()).isEqualTo("Club");
        assertThat(team.getMembers()).containsExactly("Reading", "Swimming", "Cooking");
    }

    @Test
    void theReturnedListIsReadOnly() {
        Team team = new Team("Club", start());
        List<String> members = team.getMembers();
        assertThatThrownBy(() -> members.add("Singing")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(members::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThat(team.getMembers()).containsExactly("Reading", "Swimming");
    }

    @Test
    void theCallersListIsNotShared() {
        List<String> start = start();
        Team team = new Team("Club", start);
        start.add("Singing");
        start.remove("Reading");
        assertThat(team.getMembers()).containsExactly("Reading", "Swimming");
    }
}
