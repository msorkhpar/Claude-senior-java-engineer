package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CarolTest {

    @Test
    void listsTheGiftsOfEachDay() {
        assertThat(Carol.gifts(3)).startsWith("three French hens").endsWith("and a partridge in a pear tree");
        assertThat(Carol.gifts(4)).startsWith("four calling birds");
    }

    @Test
    void theFirstDayHasOneGift() {
        assertThat(Carol.gifts(1)).containsExactly("a partridge in a pear tree");
    }

    @Test
    void everyEarlierGiftIsRepeated() {
        assertThat(Carol.gifts(2)).containsExactly("two turtle doves", "and a partridge in a pear tree");
        assertThat(Carol.gifts(4)).containsExactly("four calling birds", "three French hens", "two turtle doves", "and a partridge in a pear tree");
    }

    @Test
    void aDayOutsideTheSongIsRefused() {
        assertThatThrownBy(() -> Carol.gifts(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Carol.gifts(5)).isInstanceOf(IllegalArgumentException.class);
    }
}
