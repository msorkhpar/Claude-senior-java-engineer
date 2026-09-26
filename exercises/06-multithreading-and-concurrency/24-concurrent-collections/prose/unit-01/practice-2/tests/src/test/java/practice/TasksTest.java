package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TasksTest {

    @Test
    void servesInArrivalOrder() {
        Tasks tasks = new Tasks();
        tasks.add("a");
        tasks.add("b");
        tasks.add("c");
        assertThat(tasks.last()).isEqualTo("c");
        assertThat(tasks.next()).isEqualTo("a");
        assertThat(tasks.next()).isEqualTo("b");
        assertThat(tasks.last()).isEqualTo("c");
        assertThat(tasks.next()).isEqualTo("c");
    }

    @Test
    void urgentGoesFirst() {
        Tasks tasks = new Tasks();
        tasks.add("a");
        tasks.add("b");
        tasks.urgent("u");
        assertThat(tasks.next()).isEqualTo("u");
        assertThat(tasks.next()).isEqualTo("a");
        assertThat(tasks.last()).isEqualTo("b");
    }

    @Test
    void newestUrgentServedFirst() {
        Tasks tasks = new Tasks();
        tasks.add("a");
        tasks.urgent("x");
        tasks.urgent("y");
        assertThat(tasks.next()).isEqualTo("y");
        assertThat(tasks.next()).isEqualTo("x");
        assertThat(tasks.next()).isEqualTo("a");
    }

    @Test
    void emptyLineGivesNull() {
        Tasks tasks = new Tasks();
        assertThat(tasks.next()).isNull();
        assertThat(tasks.last()).isNull();
        tasks.add("a");
        assertThat(tasks.next()).isEqualTo("a");
        assertThat(tasks.next()).isNull();
    }

    @Test
    void urgentTasksAreInTheSameLine() {
        Tasks line = new Tasks();
        line.urgent("x");
        assertThat(line.last()).as("an urgent task alone is also the back of the line").isEqualTo("x");
        line.urgent("y");
        assertThat(line.last()).isEqualTo("x");
        line.add("a");
        assertThat(line.last()).isEqualTo("a");
        assertThat(line.next()).isEqualTo("y");
    }
}
