package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class DispatcherTest {

    public static class Inventory {
        public String reserve(String sku, int quantity) {
            if (quantity <= 0) throw new IllegalArgumentException("quantity must be at least 1, was " + quantity);
            return quantity + " x " + sku + " reserved";
        }

        public int stock(String sku) {
            return sku.length() * 3;
        }

        public void recount(String aisle) throws IOException {
            throw new IOException("scanner offline in aisle " + aisle);
        }

        public void close(String aisle) {
            throw new IllegalStateException("aisle " + aisle + " has open picks");
        }
    }

    private final Dispatcher console = new Dispatcher(new Inventory());

    @Test
    void aCommandThatCompletesIs200AndOneThatFailsIs500NamingWhatItThrew() {
        assertThat(console.call("reserve", "SKU-7731", 12))
                .isEqualTo(new Dispatcher.Reply(200, "12 x SKU-7731 reserved"));
        assertThat(console.call("stock", "AB-12")).isEqualTo(new Dispatcher.Reply(200, "15"));
        assertThat(console.call("recount", "C4")).isEqualTo(new Dispatcher.Reply(500, "IOException"));
        assertThat(console.call("close", "C4")).isEqualTo(new Dispatcher.Reply(500, "IllegalStateException"));
    }

    @Test
    void aCommandRejectingItsInputIs422WithTheCommandsMessage() {
        assertThat(console.call("reserve", "SKU-7731", -3))
                .isEqualTo(new Dispatcher.Reply(422, "quantity must be at least 1, was -3"));
    }

    @Test
    void argumentsThatDontFitTheCommandAre400() {
        Dispatcher.Reply wrongType = console.call("reserve", "SKU-7731", "twelve");
        Dispatcher.Reply missing = console.call("reserve", "SKU-7731");

        assertThat(wrongType.status()).isEqualTo(400);
        assertThat(wrongType.body()).startsWith("bad arguments");
        assertThat(missing.status()).isEqualTo(400);
        assertThat(missing.body()).startsWith("bad arguments");
    }
}
