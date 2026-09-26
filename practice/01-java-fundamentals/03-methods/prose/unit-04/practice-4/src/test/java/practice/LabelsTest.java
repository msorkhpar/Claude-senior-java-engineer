package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LabelsTest {

    @Test
    void labelsByDeclaredType() {
        assertThat(Labels.label("hi")).isEqualTo("text of 2");
        assertThat(Labels.label(42)).isEqualTo("object: 42");
        assertThat(Labels.label("")).isEqualTo("text of 0");
    }

    @Test
    void theStaticTypeDecides() {
        Object o = "hi";
        assertThat(Labels.label(o)).isEqualTo("object: hi");
    }

    @Test
    void nullGoesToTheMostSpecificOverload() {
        assertThat(Labels.label(null)).isEqualTo("no text");
        assertThat(Labels.label((String) null)).isEqualTo("no text");
        assertThat(Labels.label((Object) null)).isEqualTo("object: null");
    }
}
