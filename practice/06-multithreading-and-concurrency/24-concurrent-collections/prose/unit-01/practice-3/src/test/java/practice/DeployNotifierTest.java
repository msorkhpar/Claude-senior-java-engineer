package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DeployNotifierTest {

    private final DeployNotifier notifier = new DeployNotifier();
    private final List<String> heard = new ArrayList<>();

    /** A subscriber that records "name:event" for every event it hears. */
    private Consumer<String> recorder(String name) {
        return event -> heard.add(name + ":" + event);
    }

    @Test
    void everySubscriberHearsEveryDeployInOrder() {
        Consumer<String> billing = recorder("billing"), search = recorder("search"), audit = recorder("audit");
        assertThat(notifier.subscribe(billing)).isTrue();
        assertThat(notifier.subscribe(search)).isTrue();
        assertThat(notifier.subscribe(audit)).isTrue();

        assertThat(notifier.deploy("v7")).isEqualTo(3);
        assertThat(notifier.deploy("v8")).isEqualTo(3);

        assertThat(heard).containsExactly(
                "billing:v7", "search:v7", "audit:v7", "billing:v8", "search:v8", "audit:v8");
    }

    @Test
    void anUnsubscribedListenerHearsNoLaterDeploys() {
        Consumer<String> billing = recorder("billing"), search = recorder("search");
        notifier.subscribe(billing);
        notifier.subscribe(search);
        notifier.deploy("v7");

        assertThat(notifier.unsubscribe(search)).isTrue();
        assertThat(notifier.deploy("v8")).isEqualTo(1);

        assertThat(heard).containsExactly("billing:v7", "search:v7", "billing:v8");
    }

    @Test
    void aOneShotListenerThatUnsubscribesItselfDoesNotDisturbTheOthers() {
        Consumer<String> oneShot = new Consumer<>() {
            public void accept(String event) {
                heard.add("one-shot:" + event);
                notifier.unsubscribe(this);
            }
        };
        notifier.subscribe(oneShot);
        notifier.subscribe(recorder("billing"));
        notifier.subscribe(recorder("audit"));

        assertThat(notifier.deploy("v41")).isEqualTo(3);
        assertThat(notifier.deploy("v42")).isEqualTo(2);

        assertThat(heard).containsExactly(
                "one-shot:v41", "billing:v41", "audit:v41", "billing:v42", "audit:v42");
    }

    @Test
    void aListenerSubscribedDuringADeployHearsFromTheNextOne() {
        Consumer<String> rollout = recorder("rollout");
        notifier.subscribe(event -> {
            heard.add("canary:" + event);
            notifier.subscribe(rollout);
        });
        notifier.subscribe(recorder("metrics"));

        assertThat(notifier.deploy("v90")).isEqualTo(2);
        assertThat(notifier.deploy("v91")).isEqualTo(3);

        assertThat(heard).containsExactly(
                "canary:v90", "metrics:v90", "canary:v91", "metrics:v91", "rollout:v91");
    }

    @Test
    void aListenerUnsubscribedByAnotherDuringADeployStillHearsThatDeploy() {
        Consumer<String> oncall = recorder("oncall");
        notifier.subscribe(event -> {
            heard.add("pager:" + event);
            notifier.unsubscribe(oncall);
        });
        notifier.subscribe(oncall);

        assertThat(notifier.deploy("v43")).isEqualTo(2);
        assertThat(notifier.deploy("v44")).isEqualTo(1);

        assertThat(heard).containsExactly("pager:v43", "oncall:v43", "pager:v44");
    }
}
