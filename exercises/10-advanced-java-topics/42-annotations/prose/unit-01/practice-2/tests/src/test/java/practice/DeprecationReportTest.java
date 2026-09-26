package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeprecationReportTest {

    public static class LegacyApi {
        @Deprecated(since = "2.0", forRemoval = true)
        public String oldMethod() {
            return "old result";
        }

        public String newMethod() {
            return "new result";
        }

        @Deprecated(since = "1.5")
        public int legacyCalculation(int a, int b) {
            return a + b;
        }

        public int modernCalculation(int a, int b) {
            return Math.addExact(a, b);
        }
    }

    public static class Connection {
        @Deprecated(since = "3.0")
        public Connection(String url) {
        }

        public Connection() {
        }

        public static Connection open(String url) {
            return new Connection();
        }
    }

    public static class Job {
        @Deprecated
        public void run() {
        }

        public void start() {
        }
    }

    public static class Modern extends LegacyApi {
        public String fresh() {
            return "fresh";
        }
    }

    @Test
    void listsTheDeprecatedMethodsWithTheirUrgency() {
        assertThat(DeprecationReport.of(LegacyApi.class)).containsExactly(
                "legacyCalculation(int, int) since 1.5",
                "oldMethod() since 2.0, for removal");
    }

    @Test
    void aDeprecatedConstructorIsListed() {
        assertThat(DeprecationReport.of(Connection.class)).containsExactly("Connection(String) since 3.0");
    }

    @Test
    void aBareDeprecatedShowsNoSince() {
        assertThat(DeprecationReport.of(Job.class)).containsExactly("run()");
    }

    @Test
    void inheritedMembersAreNotListed() {
        assertThat(DeprecationReport.of(Modern.class)).isEmpty();
    }
}
