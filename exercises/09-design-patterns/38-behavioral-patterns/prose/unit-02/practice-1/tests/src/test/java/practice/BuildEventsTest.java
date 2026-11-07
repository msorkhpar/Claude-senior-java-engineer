package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class BuildEventsTest {

    private final BuildEvents events = new BuildEvents();
    private final List<String> heard = new ArrayList<>();

    private Consumer<String> plugin(String name) {
        return build -> heard.add(name + ":" + build);
    }

    /** A plug-in that wants only one build and then unsubscribes itself. */
    private Consumer<String> oneShot(String name) {
        return new Consumer<>() {
            @Override
            public void accept(String build) {
                heard.add(name + ":" + build);
                events.unsubscribe(this);
            }
        };
    }

    @Test
    void everyPluginHearsEveryBuild() {
        events.subscribe(plugin("chat"));
        events.subscribe(plugin("dashboard"));
        events.subscribe(plugin("cleaner"));

        events.publish("b-101");
        events.publish("b-102");

        assertThat(heard).containsExactly(
                "chat:b-101", "dashboard:b-101", "cleaner:b-101",
                "chat:b-102", "dashboard:b-102", "cleaner:b-102");
    }

    @Test
    void aPluginThatLeavesFirstCostsNobodyTheBuild() {
        events.subscribe(oneShot("release-notes"));
        events.subscribe(plugin("chat"));
        events.subscribe(plugin("dashboard"));

        assertThatCode(() -> events.publish("b-207")).doesNotThrowAnyException();
        assertThatCode(() -> events.publish("b-208")).doesNotThrowAnyException();

        assertThat(heard).containsExactly(
                "release-notes:b-207", "chat:b-207", "dashboard:b-207",
                "chat:b-208", "dashboard:b-208");
        assertThat(events.listenerCount()).isEqualTo(2);
    }

    @Test
    void thePluginAfterOneThatLeavesStillHearsTheBuild() {
        events.subscribe(plugin("chat"));
        events.subscribe(oneShot("release-notes"));
        events.subscribe(plugin("dashboard"));
        events.subscribe(plugin("cleaner"));

        assertThatCode(() -> events.publish("b-330")).doesNotThrowAnyException();

        assertThat(heard).containsExactly(
                "chat:b-330", "release-notes:b-330", "dashboard:b-330", "cleaner:b-330");
    }
}
