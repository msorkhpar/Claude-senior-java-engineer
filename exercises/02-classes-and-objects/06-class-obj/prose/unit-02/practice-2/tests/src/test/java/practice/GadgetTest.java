package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GadgetTest {

    @Test
    void everyConstructorSetsItsFields() {
        Gadget plain = new Gadget();
        assertThat(plain.getName()).isEqualTo("unnamed");
        assertThat(plain.getColour()).isEqualTo("grey");
        Gadget lamp = new Gadget("lamp");
        assertThat(lamp.getName()).isEqualTo("lamp");
        assertThat(lamp.getColour()).isEqualTo("grey");
        Gadget red = new Gadget("lamp", "red");
        assertThat(red.getColour()).isEqualTo("red");
        assertThat(red.steps()).contains("setup", "named:lamp");
    }

    @Test
    void theSharedSetupRunsOnceWhateverTheConstructor() {
        assertThat(new Gadget().steps()).containsOnlyOnce("setup").hasSize(2);
        assertThat(new Gadget("lamp").steps()).containsOnlyOnce("setup").hasSize(2);
        assertThat(new Gadget("lamp", "red").steps()).containsOnlyOnce("setup").hasSize(2);
    }

    @Test
    void theSharedSetupRunsBeforeAnyConstructorBody() {
        assertThat(new Gadget("lamp", "red").steps()).containsExactly("setup", "named:lamp");
        assertThat(new Gadget().steps()).containsExactly("setup", "named:unnamed");
    }
}
