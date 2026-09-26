package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyTest {

    @Test
    void returnsOneInstancePerCode() {
        Currency euro = Currency.of("EUR");

        assertThat(Currency.of("EUR")).isSameAs(euro);
        assertThat(euro.code()).isEqualTo("EUR");
        assertThat(Currency.of("JPY")).isNotSameAs(euro);
    }

    @Test
    void aCodeBuiltAtRunTimeGetsTheCachedInstance() {
        Currency literal = Currency.of("GBP");
        String built = new StringBuilder("G").append("BP").toString();

        assertThat(Currency.of(built)).isSameAs(literal);
    }

    @Test
    void aMalformedCodeIsRefused() {
        assertThatThrownBy(() -> Currency.of("EURO")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Currency.of("eu")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Currency.of("E1R")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Currency.of("usd")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Currency.of("EUR\n")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Currency.of("\u00c4\u00d6\u00dc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Currency.of(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
