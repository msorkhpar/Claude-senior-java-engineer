package practice;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DistinctTest {

    @Test
    void countsDifferentWords() {
        assertThat(Distinct.count(List.of("apple", "pear", "apple"))).isEqualTo(2);
        assertThat(Distinct.count(List.of())).isZero();
    }

    @Test
    void caseDoesNotMakeANewWord() {
        assertThat(Distinct.count(List.of("Apple", "APPLE", "apple"))).isEqualTo(1);
    }

    @Test
    void spacesDoNotMakeANewWord() {
        assertThat(Distinct.count(List.of(" pear", "pear ", "pear"))).isEqualTo(1);
    }
}
