package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumsTest {

    enum Dock { NORTH_GATE, SOUTH_GATE, COLD_STORE }

    enum Handling { FRAGILE, PERISHABLE, OVERSIZE, HAZMAT }

    enum Stage {
        RECEIVED,
        PICKING {
            @Override
            boolean needsScanner() {
                return true;
            }
        },
        PACKED,
        SHIPPED;

        boolean needsScanner() {
            return false;
        }
    }

    @Test
    void exactNamesParseForEveryEnum() {
        assertThat(Enums.parse(Dock.class, "COLD_STORE")).isEqualTo(Dock.COLD_STORE);
        assertThat(Enums.parse(Handling.class, "HAZMAT")).isEqualTo(Handling.HAZMAT);
        assertThat(Enums.parse(Stage.class, "PICKING")).isEqualTo(Stage.PICKING);
    }

    @Test
    void textThatNamesNoConstantIsRefused() {
        assertThatThrownBy(() -> Enums.parse(Dock.class, "EAST_GATE"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Enums.parse(Handling.class, "fragil"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCellReadsEveryFlagItNamesOnce() {
        assertThat(Enums.parseAll(Handling.class, "HAZMAT,FRAGILE,HAZMAT"))
                .containsExactlyInAnyOrder(Handling.FRAGILE, Handling.HAZMAT);
    }

    @Test
    void nextWalksAnEnumAndWrapsRound() {
        assertThat(Enums.next(Dock.NORTH_GATE)).isEqualTo(Dock.SOUTH_GATE);
        assertThat(Enums.next(Dock.COLD_STORE)).isEqualTo(Dock.NORTH_GATE);
        assertThat(Enums.next(Handling.OVERSIZE)).isEqualTo(Handling.HAZMAT);
    }

    @Test
    void spellingsPeopleTypeParse() {
        assertThat(Enums.parse(Dock.class, "north-gate")).isEqualTo(Dock.NORTH_GATE);
        assertThat(Enums.parse(Dock.class, " Cold Store ")).isEqualTo(Dock.COLD_STORE);
        assertThat(Enums.parse(Handling.class, "Perishable")).isEqualTo(Handling.PERISHABLE);
    }

    @Test
    void aCellsFlagsComeBackInDeclarationOrder() {
        assertThat(Enums.parseAll(Handling.class, "HAZMAT,OVERSIZE,FRAGILE"))
                .containsExactly(Handling.FRAGILE, Handling.OVERSIZE, Handling.HAZMAT);
    }

    @Test
    void nextWorksFromAConstantWithItsOwnBody() {
        assertThat(Enums.next(Stage.RECEIVED)).isEqualTo(Stage.PICKING);
        assertThat(Enums.next(Stage.PICKING)).isEqualTo(Stage.PACKED);
    }
}
