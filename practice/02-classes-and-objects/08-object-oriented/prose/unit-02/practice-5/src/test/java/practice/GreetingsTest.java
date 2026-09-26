package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingsTest {

    @Test
    void greetAllGreetsEveryName() {
        Greetings.Greeter english = new Greetings.English();
        assertThat(english.greet("Ann")).isEqualTo("Hello, Ann!");
        assertThat(english.greetAll(List.of("Ann", "Bo"))).containsExactly("Hello, Ann!", "Hello, Bo!");
    }

    @Test
    void theDefaultUsesEachImplementersGreeting() {
        Greetings.Greeter french = new Greetings.French();
        assertThat(french.greetAll(List.of("Ann", "Bo"))).containsExactly("Bonjour, Ann !", "Bonjour, Bo !");
    }

    @Test
    void anImplementerMayOverrideTheDefault() {
        Greetings.Greeter shy = new Greetings.Shy();
        assertThat(shy.greetAll(List.of("Ann", "Bo"))).containsExactly("hi Ann");
    }

    @Test
    void noNamesGiveNoGreetings() {
        assertThat(new Greetings.English().greetAll(List.of())).isEmpty();
        assertThat(new Greetings.Shy().greetAll(List.of())).isEmpty();
    }
}
