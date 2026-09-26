package practice;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings({"deprecation", "removal"})
class LegacyApiTest {

    private final LegacyApi api = new LegacyApi();

    @Test
    void oldAndNewMethodsBothWork() {
        assertThat(api.oldMethod()).isEqualTo("old result");
        assertThat(api.newMethod()).isEqualTo("new result");
        assertThat(api.legacyCalculation(2, 3)).isEqualTo(5);
        assertThat(api.legacyCalculation(Integer.MAX_VALUE, 1)).isEqualTo(Integer.MIN_VALUE);
        assertThat(api.modernCalculation(2, 3)).isEqualTo(5);
        assertThat(api.modernCalculation(-700, 200)).isEqualTo(-500);
    }

    @Test
    void oldMethodIsDeprecatedForRemoval() throws Exception {
        Deprecated d = LegacyApi.class.getMethod("oldMethod").getAnnotation(Deprecated.class);

        assertThat(d).isNotNull();
        assertThat(d.since()).isEqualTo("2.0");
        assertThat(d.forRemoval()).isTrue();
        assertThat(LegacyApi.class.getMethod("newMethod").isAnnotationPresent(Deprecated.class)).isFalse();
    }

    @Test
    void legacyCalculationIsDeprecatedButStays() throws Exception {
        Method legacy = LegacyApi.class.getMethod("legacyCalculation", int.class, int.class);
        Deprecated d = legacy.getAnnotation(Deprecated.class);

        assertThat(d).isNotNull();
        assertThat(d.since()).isEqualTo("1.5");
        assertThat(d.forRemoval()).isFalse();
        Method modern = LegacyApi.class.getMethod("modernCalculation", int.class, int.class);
        assertThat(modern.isAnnotationPresent(Deprecated.class)).isFalse();
    }

    @Test
    void modernCalculationRefusesOverflow() {
        assertThatThrownBy(() -> api.modernCalculation(Integer.MAX_VALUE, 1))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> api.modernCalculation(Integer.MIN_VALUE, -1))
                .isInstanceOf(ArithmeticException.class);
    }
}
