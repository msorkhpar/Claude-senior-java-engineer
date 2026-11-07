package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DeliverySenderTest {

    private final List<String> sent = new ArrayList<>();

    private static DeliveryRules.Message msg(String id, String domain, int sizeKb) {
        return new DeliveryRules.Message(id, domain, sizeKb);
    }

    /** Hands out the given versions of the rules, one per call, and the last one from then on. */
    private static Supplier<DeliveryRules.Rules> versions(DeliveryRules.Rules... rules) {
        Iterator<DeliveryRules.Rules> it = List.of(rules).iterator();
        DeliveryRules.Rules[] last = {rules[0]};
        return () -> it.hasNext() ? (last[0] = it.next()) : last[0];
    }

    @Test
    void sendsWhatTheRulesAllowAndSkipsTheRest() {
        DeliveryRules.Store store = new DeliveryRules.Store();
        store.block("bulk.example.net");
        store.setMaxSizeKb("example.org", 500);
        DeliveryRules.Sender sender = new DeliveryRules.Sender(store::current, m -> sent.add(m.id()));

        int count = sender.sendAll(List.of(
                msg("m-101", "example.org", 300),
                msg("m-102", "bulk.example.net", 12),
                msg("m-103", "example.org", 900),
                msg("m-104", "example.com", 9_000)));

        assertThat(sent).containsExactly("m-101", "m-104");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void aBlockMadeWhileABatchIsSendingStopsTheRestOfTheBatch() {
        DeliveryRules.Store store = new DeliveryRules.Store();
        DeliveryRules.Sender sender = new DeliveryRules.Sender(store::current, m -> {
            sent.add(m.id());
            if (m.id().equals("q-1")) store.block("partner.example.com");   // the admin blocks it now
        });

        sender.sendAll(List.of(
                msg("q-1", "partner.example.com", 40),
                msg("q-2", "partner.example.com", 40),
                msg("q-3", "example.net", 40),
                msg("q-4", "partner.example.com", 40)));

        assertThat(sent).containsExactly("q-1", "q-3");
    }

    @Test
    void aChangeMadeBetweenTwoBatchesAppliesToTheNextBatch() {
        DeliveryRules.Store store = new DeliveryRules.Store();
        DeliveryRules.Sender sender = new DeliveryRules.Sender(store::current, m -> sent.add(m.id()));
        sender.sendAll(List.of(msg("r-1", "news.example.org", 700)));

        store.setMaxSizeKb("news.example.org", 256);
        sender.sendAll(List.of(msg("r-2", "news.example.org", 700), msg("r-3", "news.example.org", 100)));

        assertThat(sent).containsExactly("r-1", "r-3");
    }

    @Test
    void oneMessageIsCheckedAgainstOneVersionOfTheRules() {
        DeliveryRules.Store sizeLimited = new DeliveryRules.Store();
        sizeLimited.setMaxSizeKb("example.org", 100);
        DeliveryRules.Store blocking = new DeliveryRules.Store();
        blocking.block("example.org");
        DeliveryRules.Rules permissive = new DeliveryRules.Store().current();

        // the rules change between any two reads: first the stricter version, then one that allows it
        new DeliveryRules.Sender(versions(sizeLimited.current(), permissive), m -> sent.add(m.id()))
                .sendAll(List.of(msg("s-1", "example.org", 150)));
        new DeliveryRules.Sender(versions(blocking.current(), permissive), m -> sent.add(m.id()))
                .sendAll(List.of(msg("s-2", "example.org", 150)));

        assertThat(sent).isEmpty();
    }
}
