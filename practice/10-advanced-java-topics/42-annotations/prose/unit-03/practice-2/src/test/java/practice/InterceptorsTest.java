package practice;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterceptorsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Idempotent { }

    @Retention(RetentionPolicy.SOURCE)
    @interface Draft { }

    @interface Traced { }

    @Retention(RetentionPolicy.CLASS)
    @interface Metered { }

    static class Shipments {
        @Idempotent
        String book() {
            return "booked";
        }

        String quote() {
            return "quoted";
        }
    }

    private final List<String> seen = new ArrayList<>();

    @Test
    void anActionRegisteredForAVisibleAnnotationRunsBeforeTheAnnotatedMethodOnly() throws Exception {
        Interceptors registry = new Interceptors();
        registry.register(Idempotent.class, m -> seen.add("idempotency key for " + m));
        registry.register(Deprecated.class, m -> seen.add("deprecated " + m));

        assertThat(registry.call(new Shipments(), "book")).isEqualTo("booked");
        assertThat(registry.call(new Shipments(), "quote")).isEqualTo("quoted");
        assertThat(seen).containsExactly("idempotency key for book");
    }

    @Test
    void anAnnotationTheCompilerDropsIsRefused() {
        Interceptors registry = new Interceptors();

        assertThatThrownBy(() -> registry.register(Draft.class, seen::add))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registry.register(SuppressWarnings.class, seen::add))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anAnnotationDeclaredWithoutRetentionIsRefusedAndTheMessageSaysCLASS() {
        Interceptors registry = new Interceptors();

        assertThatThrownBy(() -> registry.register(Traced.class, seen::add))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CLASS");
    }

    @Test
    void anAnnotationKeptOnlyInTheClassFileIsRefused() {
        Interceptors registry = new Interceptors();

        assertThatThrownBy(() -> registry.register(Metered.class, seen::add))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CLASS");
    }
}
