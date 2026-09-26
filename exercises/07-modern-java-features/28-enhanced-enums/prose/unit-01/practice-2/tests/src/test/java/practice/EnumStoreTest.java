package practice;

import org.junit.jupiter.api.Test;

import practice.EnumStore.StatusV1;
import practice.EnumStore.StatusV2;

import static org.assertj.core.api.Assertions.assertThat;

class EnumStoreTest {

    /** A text as it comes back from storage: a fresh String, never an interned literal. */
    private static String fromStorage(String text) {
        return new StringBuilder(text.length()).append(text).toString();
    }

    @Test
    void roundTripsAConstant() {
        assertThat(EnumStore.load(StatusV1.class, EnumStore.store(StatusV1.INACTIVE))).contains(StatusV1.INACTIVE);
        assertThat(EnumStore.load(StatusV1.class, EnumStore.store(StatusV1.ACTIVE))).contains(StatusV1.ACTIVE);
    }

    @Test
    void survivesAnAddedConstant() {
        assertThat(EnumStore.load(StatusV2.class, EnumStore.store(StatusV1.ACTIVE))).contains(StatusV2.ACTIVE);
        assertThat(EnumStore.load(StatusV2.class, EnumStore.store(StatusV1.INACTIVE))).contains(StatusV2.INACTIVE);
    }

    @Test
    void readsTextBuiltAtRuntime() {
        String stored = fromStorage(EnumStore.store(StatusV1.INACTIVE));
        assertThat(EnumStore.load(StatusV1.class, stored)).contains(StatusV1.INACTIVE);
        assertThat(EnumStore.load(StatusV2.class, fromStorage(EnumStore.store(StatusV2.PENDING)))).contains(StatusV2.PENDING);
    }

    @Test
    void unknownTextIsEmpty() {
        assertThat(EnumStore.load(StatusV2.class, fromStorage("DELETED"))).isEmpty();
        assertThat(EnumStore.load(StatusV1.class, fromStorage("PENDING"))).isEmpty();
        assertThat(EnumStore.load(StatusV1.class, fromStorage("active"))).isEmpty();
        assertThat(EnumStore.load(StatusV1.class, fromStorage(" ACTIVE "))).isEmpty();
    }

    @Test
    void nullIsEmpty() {
        assertThat(EnumStore.load(StatusV1.class, null)).isEmpty();
    }
}
