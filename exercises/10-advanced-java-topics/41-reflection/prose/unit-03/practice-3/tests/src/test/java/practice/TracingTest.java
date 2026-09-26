package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TracingTest {

    interface Greeter {
        String greet(String name);

        int size();

        void fail(String why);
    }

    static class RealGreeter implements Greeter {
        @Override
        public String greet(String name) {
            return "Hello, " + name;
        }

        @Override
        public int size() {
            return 3;
        }

        @Override
        public void fail(String why) {
            throw new IllegalArgumentException(why);
        }
    }

    @Test
    void tracesACall() {
        List<String> log = new ArrayList<>();
        Greeter greeter = Tracing.trace(Greeter.class, new RealGreeter(), log);

        assertThat(greeter.greet("World")).isEqualTo("Hello, World");
        assertThat(log).containsExactly("greet(World)", "greet -> Hello, World");
    }

    @Test
    void aZeroArgumentCallIsTraced() {
        List<String> log = new ArrayList<>();
        Greeter greeter = Tracing.trace(Greeter.class, new RealGreeter(), log);

        assertThat(greeter.size()).isEqualTo(3);
        assertThat(log).containsExactly("size()", "size -> 3");
    }

    @Test
    void theTargetsExceptionPassesThrough() {
        List<String> log = new ArrayList<>();
        Greeter greeter = Tracing.trace(Greeter.class, new RealGreeter(), log);

        assertThatThrownBy(() -> greeter.fail("bad"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bad");
        assertThat(log).containsExactly("fail(bad)", "fail !! IllegalArgumentException");
    }

    @Test
    void aProxyEqualsItself() {
        List<String> log = new ArrayList<>();
        RealGreeter real = new RealGreeter();
        Greeter greeter = Tracing.trace(Greeter.class, real, log);

        assertThat(greeter.equals(greeter)).isTrue();
        assertThat(greeter.equals(new RealGreeter())).isFalse();
        assertThat(greeter.hashCode()).isEqualTo(System.identityHashCode(greeter));
        assertThat(greeter.toString()).isEqualTo("Traced(" + real + ")");
        assertThat(log).isEmpty();
    }
}
