package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PalindromeTest {

    @Test
    void recognisesPalindromes() {
        assertThat(Palindrome.isPalindrome("level")).isTrue();
        assertThat(Palindrome.isPalindrome("abba")).isTrue();
        assertThat(Palindrome.isPalindrome("hello")).isFalse();
    }

    @Test
    void theInnerPairIsChecked() {
        assertThat(Palindrome.isPalindrome("abca")).isFalse();
        assertThat(Palindrome.isPalindrome("zabcaz")).isFalse();
    }

    @Test
    void caseIsIgnored() {
        assertThat(Palindrome.isPalindrome("Level")).isTrue();
        assertThat(Palindrome.isPalindrome("AbBa")).isTrue();
    }

    @Test
    void shortStringsArePalindromes() {
        assertThat(Palindrome.isPalindrome("")).isTrue();
        assertThat(Palindrome.isPalindrome("x")).isTrue();
    }
}
