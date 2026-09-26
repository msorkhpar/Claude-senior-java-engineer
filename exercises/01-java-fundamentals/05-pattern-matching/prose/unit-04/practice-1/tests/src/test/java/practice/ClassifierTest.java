package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ClassifierTest {

    @Test
    void classifiesNumbers() {
        assertThat(Classifier.classify(-5)).isEqualTo("Negative integer");
        assertThat(Classifier.classify(42)).isEqualTo("Small positive integer");
        assertThat(Classifier.classify(5000)).isEqualTo("Large positive integer");
        assertThat(Classifier.classify(2.5)).isEqualTo("Finite double");
        assertThat(Classifier.classify("7")).isEqualTo("Not a number type");
    }

    @Test
    void zeroHasItsOwnCase() {
        assertThat(Classifier.classify(0)).isEqualTo("Zero");
    }

    @Test
    void oneHundredIsStillSmall() {
        assertThat(Classifier.classify(100)).isEqualTo("Small positive integer");
        assertThat(Classifier.classify(101)).isEqualTo("Large positive integer");
        assertThat(Classifier.classify(1)).isEqualTo("Small positive integer");
    }

    @Test
    void specialDoublesAreNamed() {
        assertThat(Classifier.classify(Double.NaN)).isEqualTo("Not a number");
        assertThat(Classifier.classify(Double.POSITIVE_INFINITY)).isEqualTo("Infinite");
        assertThat(Classifier.classify(Double.NEGATIVE_INFINITY)).isEqualTo("Infinite");
    }
}
