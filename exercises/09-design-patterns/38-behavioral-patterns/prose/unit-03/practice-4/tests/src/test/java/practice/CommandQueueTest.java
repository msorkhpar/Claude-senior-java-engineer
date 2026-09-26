package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandQueueTest {

    @Test
    void runsQueuedCommandsLater() {
        List<String> log = new ArrayList<>();
        CommandQueue queue = new CommandQueue();
        queue.enqueue(() -> log.add("save"));
        queue.enqueue(() -> log.add("send"));
        queue.enqueue(() -> log.add("sync"));

        assertThat(log).isEmpty();
        assertThat(queue.size()).isEqualTo(3);
        assertThat(queue.executeNext()).isTrue();
        assertThat(log).hasSize(1);
        assertThat(queue.executeAll()).isEqualTo(2);
        assertThat(log).containsExactlyInAnyOrder("save", "send", "sync");
        assertThat(queue.isEmpty()).isTrue();
        assertThatThrownBy(() -> queue.enqueue(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void commandsRunInTheOrderTheyArrived() {
        List<String> log = new ArrayList<>();
        CommandQueue queue = new CommandQueue();
        queue.enqueue(() -> log.add("a"));
        queue.enqueue(() -> log.add("b"));
        queue.enqueue(() -> log.add("c"));

        queue.executeNext();
        queue.executeAll();

        assertThat(log).containsExactly("a", "b", "c");
    }

    @Test
    void executeNextOnAnEmptyQueueReturnsFalse() {
        CommandQueue queue = new CommandQueue();

        assertThat(queue.executeNext()).isFalse();
        assertThat(queue.executeAll()).isZero();
    }

    @Test
    void aFailingCommandLeavesTheRestQueued() {
        List<String> log = new ArrayList<>();
        CommandQueue queue = new CommandQueue();
        queue.enqueue(() -> {
            throw new IllegalStateException("disk full");
        });
        queue.enqueue(() -> log.add("b"));
        queue.enqueue(() -> log.add("c"));

        assertThatThrownBy(queue::executeAll).isInstanceOf(IllegalStateException.class);
        assertThat(queue.size()).isEqualTo(2);
        assertThat(queue.executeAll()).isEqualTo(2);
        assertThat(log).containsExactly("b", "c");
    }

    @Test
    void commandsQueuedWhileRunningAlsoRun() {
        List<String> log = new ArrayList<>();
        CommandQueue queue = new CommandQueue();
        queue.enqueue(() -> {
            log.add("a");
            queue.enqueue(() -> log.add("d"));
        });
        queue.enqueue(() -> log.add("b"));

        assertThat(queue.executeAll()).isEqualTo(3);
        assertThat(log).containsExactly("a", "b", "d");
        assertThat(queue.isEmpty()).isTrue();
    }
}
