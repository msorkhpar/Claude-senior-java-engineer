package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void validValuesAreKept() {
        Member grace = new Member("Grace", 85);
        assertThat(grace.getName()).isEqualTo("Grace");
        assertThat(grace.getAge()).isEqualTo(85);
    }

    @Test
    void aBlankNameIsRefused() {
        assertThatThrownBy(() -> new Member("", 30)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Member("   ", 30)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aNullNameIsRefusedTheSameWay() {
        assertThatThrownBy(() -> new Member(null, 30)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theAgeBoundsAreAllowed() {
        assertThat(new Member("Newborn", 0).getAge()).isZero();
        assertThat(new Member("Elder", 150).getAge()).isEqualTo(150);
    }

    @Test
    void anAgeOutsideTheRangeIsRefused() {
        assertThatThrownBy(() -> new Member("Tim", -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Member("Tim", 151)).isInstanceOf(IllegalArgumentException.class);
    }
}
