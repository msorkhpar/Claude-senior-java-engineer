package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FieldsTest {

    @Test
    void stripsAndDropsBlankValues() {
        assertThat(Fields.clean(List.of(" a ", "b\t", "   ", "", "\t\n", "\n c d \n"))).containsExactly("a", "b", "c d");
        assertThat(Fields.clean(List.of())).isEmpty();
    }

    @Test
    void unicodeSpacesAreStripped() {
        assertThat(Fields.clean(List.of(" Hello ", "  x　"))).containsExactly("Hello", "x");
    }

    @Test
    void unicodeOnlyValuesAreBlank() {
        assertThat(Fields.clean(List.of("  ", "x", "   "))).containsExactly("x");
    }
}
