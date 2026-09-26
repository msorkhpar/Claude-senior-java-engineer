package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassLookupTest {

    static final AtomicInteger QUIET_RUNS = new AtomicInteger();
    static final AtomicInteger LOUD_RUNS = new AtomicInteger();

    static class Quiet {
        static {
            QUIET_RUNS.incrementAndGet();
        }
    }

    static class Loud {
        static {
            LOUD_RUNS.incrementAndGet();
        }
    }

    static class Outer {
        static class Inner {
        }
    }

    @Test
    void threeWaysGiveOneClassObject() {
        List<String> list = new ArrayList<>();

        Class<?> loaded = ClassLookup.load("java.util.ArrayList", true).orElseThrow();

        assertThat(loaded).isSameAs(ArrayList.class).isSameAs(list.getClass());
        assertThat(ClassLookup.nameFor(ArrayList.class)).isEqualTo("java.util.ArrayList");
    }

    @Test
    void aMissingClassIsEmpty() {
        assertThat(ClassLookup.load("practice.NoSuchType", true)).isEmpty();
    }

    @Test
    void loadingWithoutInitializingRunsNoStaticInitializer() {
        Class<?> loaded = ClassLookup.load("practice.ClassLookupTest$Quiet", false).orElseThrow();

        assertThat(loaded.getSimpleName()).isEqualTo("Quiet");
        assertThat(QUIET_RUNS.get()).isZero();
    }

    @Test
    void loadingWithInitializeRunsTheStaticInitializer() {
        Class<?> loaded = ClassLookup.load("practice.ClassLookupTest$Loud", true).orElseThrow();

        assertThat(loaded.getSimpleName()).isEqualTo("Loud");
        assertThat(LOUD_RUNS.get()).isEqualTo(1);
    }

    @Test
    void aNestedClassNameRoundTrips() {
        String name = ClassLookup.nameFor(Outer.Inner.class);

        assertThat(name).isEqualTo("practice.ClassLookupTest$Outer$Inner");
        assertThat(ClassLookup.load(name, false)).containsSame(Outer.Inner.class);
    }
}
