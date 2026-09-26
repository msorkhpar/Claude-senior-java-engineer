package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RosterTest {

    private static final Person CHARLIE = new Person(fresh("Charlie"), 30, fresh("HR"));
    private static final Person ALICE = new Person(fresh("Alice"), 25, fresh("Engineering"));
    private static final Person BOB = new Person(fresh("Bob"), 25, fresh("Engineering"));
    private static final Person DAVID = new Person(fresh("David"), 35, fresh("Engineering"));

    private static String fresh(String text) {
        return new StringBuilder(text).toString();
    }

    @Test
    void sortsByDepartmentThenAgeThenName() {
        List<Person> people = new ArrayList<>(List.of(CHARLIE, ALICE, BOB, DAVID));

        assertThat(Roster.sorted(people)).containsExactly(ALICE, BOB, DAVID, CHARLIE);

        List<Person> byAge = new ArrayList<>(List.of(ALICE, DAVID, CHARLIE));
        byAge.sort(Roster.oldestFirst());
        assertThat(byAge).containsExactly(DAVID, CHARLIE, ALICE);
    }

    @Test
    void equalDepartmentAndAgeFallBackToName() {
        Person zoe = new Person(fresh("Zoe"), 25, fresh("Engineering"));
        Person adam = new Person(fresh("Adam"), 25, fresh("Engineering"));

        assertThat(Roster.sorted(List.of(zoe, adam))).containsExactly(adam, zoe);
        assertThat(Roster.byDepartmentAgeName().compare(zoe, adam)).isPositive();
    }

    @Test
    void nullEntriesComeFirst() {
        assertThat(Roster.sorted(Arrays.asList(BOB, null, ALICE))).containsExactly(null, ALICE, BOB);
    }

    @Test
    void sortedReturnsANewListAndLeavesTheInputAlone() {
        List<Person> people = new ArrayList<>(List.of(CHARLIE, ALICE, BOB, DAVID));

        List<Person> result = Roster.sorted(people);

        assertThat(result).isNotSameAs(people);
        assertThat(people).containsExactly(CHARLIE, ALICE, BOB, DAVID);
    }

    @Test
    void theComparatorIsBuiltOnce() {
        assertThat(Roster.byDepartmentAgeName()).isSameAs(Roster.byDepartmentAgeName());
    }
}
