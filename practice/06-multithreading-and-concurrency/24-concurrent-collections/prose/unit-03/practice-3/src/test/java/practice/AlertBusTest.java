package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlertBusTest {

    private final AlertBus bus = new AlertBus();
    private final List<String> got = new ArrayList<>();

    /** A subscriber that records what it got, then runs an optional action. */
    private AlertBus.Subscriber named(String name, Runnable then) {
        return alert -> {
            got.add(name + ":" + alert);
            then.run();
        };
    }

    private AlertBus.Subscriber named(String name) {
        return named(name, () -> { });
    }

    @Test
    void everySubscriberGetsEachAlertOnceInOrder() {
        bus.subscribe(named("pager"));
        bus.subscribe(named("chat"));
        bus.subscribe(named("ticket"));

        bus.publish("disk-full db-7");
        bus.publish("cpu-high web-3");

        assertThat(got).containsExactly(
                "pager:disk-full db-7", "chat:disk-full db-7", "ticket:disk-full db-7",
                "pager:cpu-high web-3", "chat:cpu-high web-3", "ticket:cpu-high web-3");
    }

    @Test
    void aSubscriberDroppedBeforeItsTurnDoesNotGetTheAlert() {
        AlertBus.Subscriber sms = named("sms");
        bus.subscribe(named("pager", () -> bus.unsubscribe(sms)));   // the pager reached someone
        bus.subscribe(named("chat"));
        bus.subscribe(sms);

        bus.publish("queue-lag q-12");

        assertThat(got).containsExactly("pager:queue-lag q-12", "chat:queue-lag q-12");
    }

    @Test
    void aSubscriberAddedDuringAnAlertStartsWithTheNextOne() {
        AlertBus.Subscriber audit = named("audit");
        bus.subscribe(named("pager", () -> bus.subscribe(audit)));
        bus.subscribe(named("chat"));

        bus.publish("cert-expiry api-2");

        assertThat(got).containsExactly("pager:cert-expiry api-2", "chat:cert-expiry api-2");
    }

    @Test
    void aSubscriberThatLeavesDuringAnAlertDoesNotCostTheNextOneIt() {
        AlertBus.Subscriber[] escalation = new AlertBus.Subscriber[1];
        escalation[0] = named("escalation", () -> bus.unsubscribe(escalation[0]));
        bus.subscribe(escalation[0]);
        bus.subscribe(named("chat"));
        bus.subscribe(named("ticket"));

        bus.publish("ack inc-88");

        assertThat(got).containsExactly("escalation:ack inc-88", "chat:ack inc-88", "ticket:ack inc-88");
    }
}
