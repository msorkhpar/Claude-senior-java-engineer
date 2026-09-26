package practice;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import practice.ClassProbe.Outcome;

import static org.assertj.core.api.Assertions.assertThat;

class ClassProbeTest {

    static final AtomicInteger COUNTED_INITS = new AtomicInteger();
    static final AtomicInteger QUIET_INITS = new AtomicInteger();
    static final AtomicInteger DORMANT_INITS = new AtomicInteger();

    static class Counted {
        static {
            COUNTED_INITS.incrementAndGet();
        }
    }

    static class Quiet {
        static {
            QUIET_INITS.incrementAndGet();
        }
    }

    static class Broken {
        static {
            if (Boolean.parseBoolean("true")) {
                throw new IllegalStateException("static init failed");
            }
        }
    }

    static class Dormant {
        static {
            DORMANT_INITS.incrementAndGet();
            if (Boolean.parseBoolean("true")) {
                throw new IllegalStateException("static init failed");
            }
        }
    }

    private static final ClassLoader LOADER = ClassProbeTest.class.getClassLoader();

    @Test
    void initialisesKnownClassesAndReportsMissingOnes() {
        assertThat(ClassProbe.initialise("java.lang.String", LOADER)).isEqualTo(Outcome.LOADED);

        assertThat(ClassProbe.initialise(Counted.class.getName(), LOADER)).isEqualTo(Outcome.LOADED);
        assertThat(ClassProbe.initialise(Counted.class.getName(), LOADER)).isEqualTo(Outcome.LOADED);
        assertThat(COUNTED_INITS).hasValue(1);

        assertThat(ClassProbe.initialise("com.example.DoesNotExist", LOADER)).isEqualTo(Outcome.NOT_FOUND);
    }

    @Test
    void aFailedInitialiserMakesTheClassUnusable() {
        String broken = ClassProbeTest.class.getName() + "$Broken";

        assertThat(ClassProbe.initialise(broken, LOADER)).isEqualTo(Outcome.INIT_FAILED);
        assertThat(ClassProbe.initialise(broken, LOADER)).isEqualTo(Outcome.UNUSABLE);
        assertThat(ClassProbe.initialise(broken, LOADER)).isEqualTo(Outcome.UNUSABLE);
    }

    @Test
    void presenceDoesNotInitialise() {
        String quiet = ClassProbeTest.class.getName() + "$Quiet";
        String dormant = ClassProbeTest.class.getName() + "$Dormant";

        assertThat(ClassProbe.isPresent(quiet, LOADER)).isTrue();
        assertThat(ClassProbe.isPresent(dormant, LOADER)).isTrue();
        assertThat(ClassProbe.isPresent("com.example.DoesNotExist", LOADER)).isFalse();
        assertThat(QUIET_INITS).hasValue(0);
        assertThat(DORMANT_INITS).hasValue(0);
    }
}
