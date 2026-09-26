package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgesTest {

    @Test
    void parsesAgesIntoOkOrErr() {
        assertThat(Ages.parseAge("42")).isEqualTo(new Ages.Ok(42));
        assertThat(Ages.parseAge("abc")).isEqualTo(new Ages.Err("not a number: abc"));
        assertThat(Ages.parseAge("200")).isEqualTo(new Ages.Err("out of range: 200"));
        assertThat(Ages.sumAges(List.of("30", "12"))).isEqualTo(new Ages.Ok(42));
        assertThat(Ages.sumAges(List.of())).isEqualTo(new Ages.Ok(0));
    }

    @Test
    void theAgeRangeIncludesBothEnds() {
        assertThat(Ages.parseAge("0")).isEqualTo(new Ages.Ok(0));
        assertThat(Ages.parseAge("150")).isEqualTo(new Ages.Ok(150));
        assertThat(Ages.parseAge("-1")).isEqualTo(new Ages.Err("out of range: -1"));
        assertThat(Ages.parseAge("151")).isEqualTo(new Ages.Err("out of range: 151"));
    }

    @Test
    void theFirstErrorWins() {
        assertThat(Ages.sumAges(List.of("1", "x", "200")))
                .isEqualTo(new Ages.Err("not a number: x"));
    }
}
