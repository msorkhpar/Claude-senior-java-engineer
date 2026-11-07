package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static practice.Session.Attribute.CART_ID;
import static practice.Session.Attribute.LAST_SEEN;
import static practice.Session.Attribute.LOCALE;
import static practice.Session.Attribute.USER_ID;

// The test that tries to change values() runs last: a solution that hands out its own list
// would otherwise break every test after it.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionTest {

    private static Object roundTrip(Object object) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(object);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return in.readObject();
        }
    }

    @Test
    @Order(1)
    void valuesListsEveryAttributeInDeclarationOrder() {
        assertThat(Session.Attribute.values()).containsExactly(USER_ID, CART_ID, LOCALE, LAST_SEEN);
    }

    @Test
    @Order(2)
    void valueOfFindsEachAttributeByName() {
        assertThat(Session.Attribute.valueOf("lastSeen")).isSameAs(LAST_SEEN);
        assertThat(Session.Attribute.valueOf("userId")).isSameAs(USER_ID);
        assertThat(Session.Attribute.valueOf("cartId")).isSameAs(CART_ID);
    }

    @Test
    @Order(3)
    void anUnknownNameIsRefusedAsTheEnumRefusedIt() {
        assertThatThrownBy(() -> Session.Attribute.valueOf("currency"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Session.Attribute.valueOf("USER_ID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(4)
    void aSessionRestoredAfterARestartStillHoldsItsValues() {
        Session session = new Session();
        session.put(USER_ID, 4_711_000_123L);
        session.put(CART_ID, new String("c-2093-b"));
        session.put(LOCALE, Locale.CANADA_FRENCH);
        session.put(LAST_SEEN, Instant.parse("2031-02-14T07:45:12Z"));

        Session restored = Session.restore(session.save());

        assertThat(restored.get(USER_ID)).isEqualTo(4_711_000_123L);
        assertThat(restored.get(CART_ID)).isEqualTo("c-2093-b");
        assertThat(restored.get(LOCALE)).isEqualTo(Locale.CANADA_FRENCH);
        assertThat(restored.get(LAST_SEEN)).isEqualTo(Instant.parse("2031-02-14T07:45:12Z"));
    }

    @Test
    @Order(5)
    void aRestoredAttributeIsTheDeclaredAttributeItself() throws Exception {
        Object restored = roundTrip(LOCALE);

        assertThat(restored).isSameAs(LOCALE);
        assertThat(roundTrip(CART_ID) == CART_ID).isTrue();
    }

    @Test
    @Order(100)
    void aCallerCannotChangeWhichAttributesExist() {
        List<Session.Attribute<?>> handedOut = Session.Attribute.values();
        try {
            handedOut.remove(LOCALE);
            handedOut.clear();
        } catch (UnsupportedOperationException refused) {
            // refusing is fine too
        }

        assertThat(Session.Attribute.values()).containsExactly(USER_ID, CART_ID, LOCALE, LAST_SEEN);
        assertThat(Session.Attribute.valueOf("locale")).isSameAs(LOCALE);
    }
}
