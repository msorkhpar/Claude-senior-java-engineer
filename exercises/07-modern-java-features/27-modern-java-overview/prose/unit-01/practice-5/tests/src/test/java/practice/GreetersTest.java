package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetersTest {

    @Test
    void standardGreeterGreets() {
        Greeters.Greeter standard = Greeters.Greeter.standard();
        assertThat(standard.greet("Ada")).isEqualTo("Hello, Ada!");
        assertThat(standard.greetWithTitle("Dr.", "Ada")).isEqualTo("Hello, Dr. Ada!");
    }

    @Test
    void titledGreetingUsesTheImplementation() {
        Greeters.Greeter hi = name -> "Hi " + name;
        assertThat(hi.greetWithTitle("Dr.", "Ada")).isEqualTo("Hi Dr. Ada");
        Greeters.Greeter loud = name -> name.toUpperCase(java.util.Locale.ROOT) + "!!";
        assertThat(loud.greetWithTitle("Prof.", "Lin")).isEqualTo("PROF. LIN!!");
    }

    @Test
    void bilingualCombinesBothDefaults() {
        assertThat(new Greeters.Bilingual().hello()).isEqualTo("Hello / Bonjour");
    }
}
