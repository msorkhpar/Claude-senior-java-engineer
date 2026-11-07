package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class WaitlistTest {

    private static Waitlist morning() {
        Waitlist w = new Waitlist();
        w.checkIn("Okafor");
        w.checkIn("Brandt");
        w.checkIn("Silva");
        w.checkIn("Haddad");
        return w;
    }

    @Test
    void listsWhoIsWaitingInArrivalOrder() {
        Waitlist w = morning();
        w.callIn("Brandt");

        assertThat(w.waiting()).containsExactly("Okafor", "Silva", "Haddad");
        assertThat(w.next(2)).containsExactly("Okafor", "Silva");
        assertThat(w.next(10)).containsExactly("Okafor", "Silva", "Haddad");
    }

    @Test
    void aScreenThatSortsAndTrimsItsListLeavesTheQueueAlone() {
        Waitlist w = morning();

        List<String> shown = w.waiting();
        shown.sort(Comparator.naturalOrder());
        shown.removeIf(p -> p.startsWith("H"));

        assertThat(shown).containsExactly("Brandt", "Okafor", "Silva");
        assertThat(w.waiting()).containsExactly("Okafor", "Brandt", "Silva", "Haddad");
        assertThat(w.size()).isEqualTo(4);
    }

    @Test
    void aNursesListStaysAsItWasWhilePatientsKeepArriving() {
        Waitlist w = morning();

        List<String> mine = w.next(2);
        w.checkIn("Novak");
        w.callIn("Silva");

        assertThat(mine).containsExactly("Okafor", "Brandt");
        assertThat(w.waiting()).containsExactly("Okafor", "Brandt", "Haddad", "Novak");
    }

    @Test
    void aNurseCanAddAWalkInToAnEmptyList() {
        Waitlist w = new Waitlist();

        List<String> mine = w.next(3);
        mine.add("Walk-in 1");
        List<String> none = morning().next(0);
        none.add("Walk-in 2");

        assertThat(mine).containsExactly("Walk-in 1");
        assertThat(none).containsExactly("Walk-in 2");
        assertThat(w.size()).isZero();
    }
}
