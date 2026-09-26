package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TasksTest {

    @Test
    void getReturnsTheTasksValue() {
        assertThat(Tasks.unchecked(() -> "hello").get()).isEqualTo("hello");
        assertThat(Tasks.unchecked(() -> 42).get()).isEqualTo(42);
    }

    @Test
    void theTaskRunsOnlyOnGetAndOnEveryGet() {
        int[] calls = {0};
        Supplier<Integer> supplier = Tasks.unchecked(() -> ++calls[0]);
        assertThat(calls[0]).isZero();

        assertThat(supplier.get()).isEqualTo(1);
        assertThat(supplier.get()).isEqualTo(2);
        assertThat(calls[0]).isEqualTo(2);
    }

    @Test
    void anIoFailureBecomesUncheckedIo() {
        IOException missing = new IOException("file.txt is missing");
        Callable<String> read = () -> {
            throw missing;
        };
        Supplier<String> supplier = Tasks.unchecked(read);

        assertThatThrownBy(supplier::get)
                .isInstanceOf(UncheckedIOException.class)
                .cause().isSameAs(missing);
    }

    @Test
    void otherCheckedFailuresKeepTheirCause() {
        TimeoutException slow = new TimeoutException("too slow");
        Callable<String> wait = () -> {
            throw slow;
        };
        Supplier<String> supplier = Tasks.unchecked(wait);

        assertThatThrownBy(supplier::get)
                .isExactlyInstanceOf(RuntimeException.class)
                .cause().isSameAs(slow);
    }

    @Test
    void runtimeFailuresPassThroughUnwrapped() {
        IllegalStateException broken = new IllegalStateException("broken");
        Callable<String> fail = () -> {
            throw broken;
        };
        Supplier<String> supplier = Tasks.unchecked(fail);

        assertThatThrownBy(supplier::get).isSameAs(broken);
    }

    @Test
    void ioSubclassesBecomeUncheckedIoToo() {
        java.io.FileNotFoundException missing = new java.io.FileNotFoundException("config.txt");
        Callable<String> open = () -> {
            throw missing;
        };
        Supplier<String> supplier = Tasks.unchecked(open);

        assertThatThrownBy(supplier::get)
                .isInstanceOf(UncheckedIOException.class)
                .cause().isSameAs(missing);
    }

    @Test
    void errorsPassThroughUntouched() {
        AssertionError broken = new AssertionError("invariant broken");
        Callable<String> fail = () -> {
            throw broken;
        };
        Supplier<String> supplier = Tasks.unchecked(fail);

        assertThatThrownBy(supplier::get).isSameAs(broken);
    }
}
