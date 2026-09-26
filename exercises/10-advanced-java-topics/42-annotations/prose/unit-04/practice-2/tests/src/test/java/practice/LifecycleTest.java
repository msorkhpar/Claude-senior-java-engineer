package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LifecycleTest {

    public static class SampleService {
        final List<String> log = new ArrayList<>();

        @Lifecycle.PostConstruct(order = 2)
        public void authenticate() {
            log.add("authenticate");
        }

        @Lifecycle.PostConstruct(order = 3)
        public void bindPorts() {
            log.add("bindPorts");
        }

        @Lifecycle.PostConstruct(order = 1)
        public void connect() {
            log.add("connect");
        }

        @Lifecycle.PostConstruct(order = 6)
        public void announce() {
            log.add("announce");
        }

        @Lifecycle.PostConstruct(order = 0)
        public void loadConfig() {
            log.add("loadConfig");
        }

        @Lifecycle.PostConstruct(order = 4)
        public void warmCaches() {
            log.add("warmCaches");
        }

        @Lifecycle.PostConstruct(order = 5)
        public void enableMetrics() {
            log.add("enableMetrics");
        }

        @Lifecycle.PreDestroy(order = 2)
        public void closeConnections() {
            log.add("closeConnections");
        }

        @Lifecycle.PreDestroy(order = 1)
        public void flushBuffers() {
            log.add("flushBuffers");
        }

        public void notACallback() {
            log.add("notACallback");
        }
    }

    /** The same callbacks with the orders reversed: no fixed method order suits both classes. */
    public static class MirroredService {
        final List<String> log = new ArrayList<>();

        @Lifecycle.PostConstruct(order = 4)
        public void authenticate() {
            log.add("authenticate");
        }

        @Lifecycle.PostConstruct(order = 3)
        public void bindPorts() {
            log.add("bindPorts");
        }

        @Lifecycle.PostConstruct(order = 5)
        public void connect() {
            log.add("connect");
        }

        @Lifecycle.PostConstruct(order = 0)
        public void announce() {
            log.add("announce");
        }

        @Lifecycle.PostConstruct(order = 6)
        public void loadConfig() {
            log.add("loadConfig");
        }

        @Lifecycle.PostConstruct(order = 2)
        public void warmCaches() {
            log.add("warmCaches");
        }

        @Lifecycle.PostConstruct(order = 1)
        public void enableMetrics() {
            log.add("enableMetrics");
        }
    }

    public static class PrivateInit {
        final List<String> log = new ArrayList<>();

        @Lifecycle.PostConstruct
        private void prepare() {
            log.add("prepare");
        }
    }

    public static class FailingInit {
        @Lifecycle.PostConstruct
        public void load() {
            throw new IllegalStateException("no config");
        }
    }

    @Test
    void runsTheCallbacksByTheirOrder() throws Exception {
        SampleService service = new SampleService();

        Lifecycle.initialize(service);
        assertThat(service.log).containsExactly(
                "loadConfig", "connect", "authenticate", "bindPorts", "warmCaches", "enableMetrics", "announce");

        MirroredService mirrored = new MirroredService();
        Lifecycle.initialize(mirrored);
        assertThat(mirrored.log).containsExactly(
                "announce", "enableMetrics", "warmCaches", "bindPorts", "authenticate", "connect", "loadConfig");

        Lifecycle.destroy(service);
        assertThat(service.log).containsExactly("loadConfig", "connect", "authenticate", "bindPorts", "warmCaches", "enableMetrics",
                "announce", "flushBuffers", "closeConnections");
    }

    @Test
    void privateCallbacksRunToo() throws Exception {
        PrivateInit bean = new PrivateInit();

        Lifecycle.initialize(bean);

        assertThat(bean.log).containsExactly("prepare");
    }

    @Test
    void aCallbackFailureArrivesUnwrapped() {
        assertThatThrownBy(() -> Lifecycle.initialize(new FailingInit()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("no config");
    }
}
