package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DeliveryRulesTest {

    @Test
    void eachChangeAppliesToTheRulesReadAfterIt() {
        DeliveryRules.Store store = new DeliveryRules.Store();

        store.block("promo.example.net");
        store.setMaxSizeKb("archive.example.org", 2_048);

        DeliveryRules.Rules rules = store.current();
        assertThat(rules.isBlocked("promo.example.net")).isTrue();
        assertThat(rules.isBlocked("example.com")).isFalse();
        assertThat(rules.maxSizeKb("archive.example.org")).isEqualTo(2_048);
        assertThat(rules.maxSizeKb("example.com")).isEqualTo(10_240);

        store.unblock("promo.example.net");
        store.setMaxSizeKb("archive.example.org", 512);

        assertThat(store.current().isBlocked("promo.example.net")).isFalse();
        assertThat(store.current().maxSizeKb("archive.example.org")).isEqualTo(512);
    }

    @Test
    void aSendThatAlreadyReadTheRulesKeepsThemThroughEveryChange() {
        DeliveryRules.Store store = new DeliveryRules.Store();
        store.block("lists.example.org");
        DeliveryRules.Rules inFlight = store.current();              // a send thread is using these

        store.block("bounce.example.com");
        store.unblock("lists.example.org");
        store.setMaxSizeKb("example.com", 64);

        assertThat(inFlight.isBlocked("bounce.example.com")).isFalse();
        assertThat(inFlight.isBlocked("lists.example.org")).isTrue();
        assertThat(inFlight.maxSizeKb("example.com")).isEqualTo(10_240);
    }

    @Test
    void sendThreadsReadTheRulesThroughAFieldThatPublishesEachChange() {
        Field[] fields = DeliveryRules.Store.class.getDeclaredFields();

        boolean published = Arrays.stream(fields).anyMatch(f ->
                (f.getType() == DeliveryRules.Rules.class && Modifier.isVolatile(f.getModifiers()))
                        || f.getType() == AtomicReference.class);
        boolean unpublished = Arrays.stream(fields).anyMatch(f ->
                f.getType() == DeliveryRules.Rules.class && !Modifier.isVolatile(f.getModifiers()));

        assertThat(published).as("the rules are held in a volatile field or an AtomicReference").isTrue();
        assertThat(unpublished).as("no plain field holds the rules").isFalse();
    }
}
