package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandleRegistryTest {

    private final HandleRegistry registry = new HandleRegistry();

    @Test
    void registersOrdinaryHandlesAsTyped() {
        assertThat(registry.register("kai_42")).isEqualTo("kai_42");
        assertThat(registry.register("marguerite")).isEqualTo("marguerite");
        assertThat(registry.register("zoë_m")).isEqualTo("zoë_m");        // precomposed e with diaeresis
        assertThat(registry.register("renée")).isEqualTo("renée");
    }

    @Test
    void refusesHandlesThatAreTooShortOrEmpty() {
        assertThatThrownBy(() -> registry.register("ok")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registry.register("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registry.register("--")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesReservedAndTakenHandles() {
        assertThatThrownBy(() -> registry.register("security")).isInstanceOf(IllegalArgumentException.class);
        registry.register("tomas_b");
        assertThatThrownBy(() -> registry.register("tomas_b")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aReservedHandleTypedInFullWidthLettersIsRefused() {
        // "ｓｕｐｐｏｒｔ": FULLWIDTH LATIN SMALL LETTERs, which render like the staff account
        assertThatThrownBy(() -> registry.register("ｓｕｐｐｏｒｔ"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aTakenHandleInAnotherWidthIsTheSameHandle() {
        assertThat(registry.register("noor")).isEqualTo("noor");
        // full-width "noor": FULLWIDTH LATIN SMALL LETTERs, Latin script and lowercase
        assertThatThrownBy(() -> registry.register("ｎｏｏｒ"))
                .isInstanceOf(IllegalArgumentException.class);
        // full-width "lior_7", low line and digit included, is the handle it spells
        assertThat(registry.register("ｌｉｏｒ＿７")).isEqualTo("lior_7");
    }

    @Test
    void aHandleWithALetterFromAnotherScriptIsRefused() {
        // Cyrillic small a (U+0430) and Greek small omicron (U+03BF) look like Latin a and o
        assertThatThrownBy(() -> registry.register("аdmin")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registry.register("rοot")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aHandleIsRefusedWhenOnlyPartOfItKeepsToTheRules() {
        assertThatThrownBy(() -> registry.register("kai.42")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registry.register("kai 42")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registry.register("abcdefghijklmnopqrstuvwxy")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registry.register("Kai_42")).isInstanceOf(IllegalArgumentException.class);
    }
}
