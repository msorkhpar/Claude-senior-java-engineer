package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartCookieTest {

    /** Counts how often a ReindexTask was rebuilt from bytes. */
    static final AtomicInteger REBUILT = new AtomicInteger();

    /** A class on the shop's class path that has no business in a cookie. Harmless here. */
    static final class ReindexTask implements Serializable {
        final String index;

        ReindexTask(String index) {
            this.index = index;
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            REBUILT.incrementAndGet();          // where a real class might start work
        }
    }

    @BeforeEach
    void resetTheCounter() {
        REBUILT.set(0);
    }

    private static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(o);
        }
        return out.toByteArray();
    }

    @Test
    void readsBackTheCartItWrote() throws Exception {
        CartCookie.Cart cart = new CartCookie.Cart("visitor-31", new ArrayList<>(List.of(
                new CartCookie.CartLine("TEA-200", 2), new CartCookie.CartLine("MUG-014", 1))));

        CartCookie.Cart back = CartCookie.read(CartCookie.write(cart));

        assertThat(back).isEqualTo(cart);
        assertThat(CartCookie.read(CartCookie.write(new CartCookie.Cart("visitor-32", new ArrayList<>()))).lines())
                .isEmpty();
    }

    @Test
    void aCookieHoldingAnotherClassIsRefusedBeforeItsCodeRuns() throws IOException {
        byte[] cookie = serialize(new ReindexTask("products"));

        assertThatThrownBy(() -> CartCookie.read(cookie)).isInstanceOf(IOException.class);
        assertThat(REBUILT.get()).isZero();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void anotherClassHiddenInsideACartIsRefusedBeforeItsCodeRuns() throws IOException {
        ArrayList lines = new ArrayList<>(List.of(new CartCookie.CartLine("TEA-200", 1)));
        lines.add(new ReindexTask("orders"));
        byte[] cookie = serialize(new CartCookie.Cart("visitor-33", lines));

        assertThatThrownBy(() -> CartCookie.read(cookie)).isInstanceOf(IOException.class);
        assertThat(REBUILT.get()).isZero();
    }
}
