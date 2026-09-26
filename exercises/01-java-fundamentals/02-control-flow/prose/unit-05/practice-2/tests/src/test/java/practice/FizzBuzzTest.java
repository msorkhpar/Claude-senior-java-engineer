package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FizzBuzzTest {

    @Test
    void playsFizzBuzz() {
        assertThat(FizzBuzz.say(15)).isEqualTo("FizzBuzz");
        assertThat(FizzBuzz.say(9)).isEqualTo("Fizz");
        assertThat(FizzBuzz.say(10)).isEqualTo("Buzz");
        assertThat(FizzBuzz.say(7)).isEqualTo("7");
        assertThat(FizzBuzz.say(33)).isEqualTo("Fizz");
    }

    @Test
    void everyMultipleOfThreeIsFizz() {
        assertThat(FizzBuzz.say(3)).isEqualTo("Fizz");
        assertThat(FizzBuzz.say(6)).isEqualTo("Fizz");
        assertThat(FizzBuzz.say(12)).isEqualTo("Fizz");
        assertThat(FizzBuzz.say(5)).isEqualTo("Buzz");
    }

    @Test
    void negativeNumbersAndZeroFollowTheRules() {
        assertThat(FizzBuzz.say(0)).isEqualTo("FizzBuzz");
        assertThat(FizzBuzz.say(-3)).isEqualTo("Fizz");
        assertThat(FizzBuzz.say(-10)).isEqualTo("Buzz");
        assertThat(FizzBuzz.say(-7)).isEqualTo("-7");
    }
}
