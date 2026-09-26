package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataConverterTest {

    @Test
    void convertsToTheRequestedType() {
        Integer i = DataConverter.STRING_TO_NUMBER.convert("42", Integer.class);
        Double d = DataConverter.STRING_TO_NUMBER.convert("3.14", Double.class);
        String s = DataConverter.TO_STRING.convert(42, String.class);
        String same = DataConverter.IDENTITY.convert("hello", String.class);
        assertThat(i).isEqualTo(42);
        assertThat(d).isEqualTo(3.14);
        assertThat(s).isEqualTo("42");
        assertThat(same).isEqualTo("hello");
        assertThat(DataConverter.STRING_TO_NUMBER.safeConvert("7", Integer.class)).contains(7);
        Float f = DataConverter.STRING_TO_NUMBER.convert("1.5", Float.class);
        assertThat(f).isEqualTo(1.5f);
        String none = DataConverter.TO_STRING.convert(null, String.class);
        assertThat(none).isEqualTo("null");
    }

    @Test
    void identityFailsInsideConvert() {
        assertThatThrownBy(() -> DataConverter.IDENTITY.convert("hello", Integer.class))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void aLongBeyondTheIntRange() {
        Long big = DataConverter.STRING_TO_NUMBER.convert("10000000000", Long.class);
        assertThat(big).isEqualTo(10_000_000_000L);
    }

    @Test
    void anUnsupportedTargetIsRefused() {
        assertThatThrownBy(() -> DataConverter.STRING_TO_NUMBER.convert("1", Boolean.class))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> DataConverter.TO_STRING.convert(1, Object.class))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void safeConvertGivesEmptyOnAnyFailure() {
        assertThat(DataConverter.STRING_TO_NUMBER.safeConvert("abc", Integer.class)).isEmpty();
        assertThat(DataConverter.STRING_TO_NUMBER.safeConvert("1", Boolean.class)).isEmpty();
        assertThat(DataConverter.IDENTITY.safeConvert("x", Integer.class)).isEmpty();
    }
}
