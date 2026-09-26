package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class KindsTest {

    @Test
    void recognisesText() {
        assertThat(Kinds.kind("hi")).isEqualTo("text");
        assertThat(Kinds.kind(" hi ")).isEqualTo("text");
        assertThat(Kinds.kind("")).isEqualTo("empty");
    }

    @Test
    void whitespaceIsBlank() {
        assertThat(Kinds.kind("   ")).isEqualTo("blank");
        assertThat(Kinds.kind("\t\n")).isEqualTo("blank");
    }

    @Test
    void emptyIsNotBlank() {
        assertThat(Kinds.kind("")).isEqualTo("empty");
        assertThat(Kinds.kind(" ")).isEqualTo("blank");
    }

    @Test
    void nullIsChecked() {
        assertThat(Kinds.kind(null)).isEqualTo("null");
    }

    @Test
    void anEmptyStringBuiltAtRunTimeIsEmpty() {
        assertThat(Kinds.kind(new String(""))).isEqualTo("empty");
        assertThat(Kinds.kind(new String(new char[0]))).isEqualTo("empty");
    }
}
