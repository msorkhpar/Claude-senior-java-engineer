package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DueTasksTest {

    private static DueTasks of(String... ids) {
        DueTasks tasks = new DueTasks();
        for (String id : ids) tasks.add(id);
        return tasks;
    }

    @Test
    void aWalkReturnsEveryTaskInOrder() {
        DueTasks tasks = of("t-301", "t-302", "t-303", "t-304", "t-305", "t-306");

        List<String> seen = new ArrayList<>();
        for (String id : tasks) seen.add(id);

        assertThat(seen).containsExactly("t-301", "t-302", "t-303", "t-304", "t-305", "t-306");
    }

    @Test
    void replacingATaskDuringAWalkDoesNotStopIt() {
        DueTasks tasks = of("t-410", "t-411", "t-412");

        List<String> seen = new ArrayList<>();
        for (String id : tasks) {
            seen.add(id);
            if (id.equals("t-410")) tasks.set(2, "t-499");
        }

        assertThat(seen).containsExactly("t-410", "t-411", "t-499");
    }

    @Test
    void theWalkCanRemoveTheTaskItEndsOn() {
        DueTasks tasks = of("t-520", "t-521", "t-522");

        Iterator<String> walk = tasks.iterator();
        while (walk.hasNext()) {
            if (walk.next().equals("t-522")) walk.remove();
        }

        assertThat(tasks.toList()).containsExactly("t-520", "t-521");
    }

    @Test
    void aTaskAddedBehindTheWalksBackFailsItsNextStep() {
        DueTasks tasks = of("t-610", "t-611", "t-612");
        Iterator<String> walk = tasks.iterator();
        walk.next();

        tasks.add("t-613");

        assertThatThrownBy(walk::next).isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void aRemovalAndAnAddBehindTheWalksBackFailItsNextStep() {
        DueTasks tasks = of("t-710", "t-711", "t-712", "t-713");
        Iterator<String> walk = tasks.iterator();
        walk.next();

        tasks.removeAt(3);
        tasks.add("t-799");

        assertThatThrownBy(walk::next).isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void theWalkCarriesOnAfterRemovingThroughItself() {
        DueTasks tasks = of("t-801", "t-802", "t-803", "t-804", "t-805");

        List<String> seen = new ArrayList<>();
        Iterator<String> walk = tasks.iterator();
        while (walk.hasNext()) {
            String id = walk.next();
            seen.add(id);
            if (id.equals("t-802")) walk.remove();
        }

        assertThat(seen).containsExactly("t-801", "t-802", "t-803", "t-804", "t-805");
        assertThat(tasks.toList()).containsExactly("t-801", "t-803", "t-804", "t-805");
    }

    @Test
    void removingTwoNeighboursThroughTheWalkVisitsAndRemovesBoth() {
        DueTasks tasks = of("t-901", "t-902", "t-903", "t-904");

        List<String> seen = new ArrayList<>();
        Iterator<String> walk = tasks.iterator();
        while (walk.hasNext()) {
            String id = walk.next();
            seen.add(id);
            if (id.equals("t-902") || id.equals("t-903")) walk.remove();
        }

        assertThat(seen).containsExactly("t-901", "t-902", "t-903", "t-904");
        assertThat(tasks.toList()).containsExactly("t-901", "t-904");
    }
}
