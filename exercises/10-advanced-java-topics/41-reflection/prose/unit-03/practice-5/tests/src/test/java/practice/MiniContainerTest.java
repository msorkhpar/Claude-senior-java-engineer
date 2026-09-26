package practice;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class MiniContainerTest {

    static class Repository {
        String data() {
            return "data from DB";
        }
    }

    static class Service {
        final Repository repo;

        @MiniContainer.Inject
        Service(Repository repo) {
            this.repo = repo;
        }

        String process() {
            return "Processed: " + repo.data();
        }
    }

    static class Report {
        final String source;

        Report() {
            this.source = "none";
        }

        @MiniContainer.Inject
        Report(Repository repo) {
            this.source = repo.data();
        }
    }

    static class Ping {
        @MiniContainer.Inject
        Ping(Pong pong) {
        }
    }

    static class Pong {
        @MiniContainer.Inject
        Pong(Ping ping) {
        }
    }

    @Test
    void resolvesAServiceWithItsRepository() {
        MiniContainer container = new MiniContainer();

        assertThat(container.resolve(Service.class).process()).isEqualTo("Processed: data from DB");
    }

    @Test
    void theSameInstanceIsShared() {
        MiniContainer container = new MiniContainer();

        Service first = container.resolve(Service.class);

        assertThat(container.resolve(Service.class)).isSameAs(first);
        assertThat(first.repo).isSameAs(container.resolve(Repository.class));
    }

    @Test
    void theInjectConstructorIsChosen() {
        MiniContainer container = new MiniContainer();

        assertThat(container.resolve(Report.class).source).isEqualTo("data from DB");
    }

    @Test
    void aCycleIsRefused() {
        MiniContainer container = new MiniContainer();

        assertThatThrownBy(() -> container.resolve(Ping.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cycle");
    }
}
