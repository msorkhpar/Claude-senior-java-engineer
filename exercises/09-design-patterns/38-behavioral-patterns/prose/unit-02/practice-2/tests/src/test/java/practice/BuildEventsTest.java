package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class BuildEventsTest {

    private record Failure(String buildId, RuntimeException error) {}

    private final List<Failure> failures = new ArrayList<>();
    private final BuildEvents events = new BuildEvents((build, e) -> failures.add(new Failure(build, e)));
    private final List<String> heard = new ArrayList<>();

    private Consumer<String> plugin(String name) {
        return build -> heard.add(name + ":" + build);
    }

    private static Consumer<String> failingWith(RuntimeException e) {
        return build -> { throw e; };
    }

    @Test
    void everyPluginHearsEveryBuild() {
        events.subscribe(plugin("chat"));
        events.subscribe(plugin("dashboard"));

        events.publish("b-401");
        events.publish("b-402");

        assertThat(heard).containsExactly("chat:b-401", "dashboard:b-401", "chat:b-402", "dashboard:b-402");
        assertThat(failures).isEmpty();
    }

    @Test
    void pluginsAfterAFailingOneStillHearTheBuild() {
        events.subscribe(plugin("chat"));
        events.subscribe(failingWith(new IllegalArgumentException("no artifact store configured")));
        events.subscribe(plugin("dashboard"));

        assertThatCode(() -> events.publish("b-412")).doesNotThrowAnyException();

        assertThat(heard).containsExactly("chat:b-412", "dashboard:b-412");
    }

    @Test
    void theOperatorHearsWhichBuildAndWhatFailed() {
        RuntimeException noStore = new IllegalArgumentException("no artifact store configured");
        RuntimeException quota = new UnsupportedOperationException("chat quota exceeded");
        events.subscribe(failingWith(noStore));
        events.subscribe(plugin("dashboard"));
        events.subscribe(failingWith(quota));

        try {
            events.publish("b-415");
        } catch (RuntimeException ignored) {
            // whether publish may fail is another case's business
        }

        assertThat(failures).hasSize(2);
        assertThat(failures.get(0).buildId()).isEqualTo("b-415");
        assertThat(failures.get(0).error()).isSameAs(noStore);
        assertThat(failures.get(1).buildId()).isEqualTo("b-415");
        assertThat(failures.get(1).error()).isSameAs(quota);
    }

    @Test
    void aFailureInTheLastPluginDoesNotFailThePublish() {
        events.subscribe(plugin("chat"));
        events.subscribe(failingWith(new IllegalArgumentException("disk full on agent-3")));

        assertThatCode(() -> events.publish("b-420")).doesNotThrowAnyException();

        assertThat(heard).containsExactly("chat:b-420");
        assertThat(failures).hasSize(1);
    }
}
