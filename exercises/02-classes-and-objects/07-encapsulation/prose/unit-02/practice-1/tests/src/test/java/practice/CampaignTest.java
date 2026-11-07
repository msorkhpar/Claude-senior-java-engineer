package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampaignTest {

    private static LocalDate oct(int day) {
        return LocalDate.of(2027, 10, day);
    }

    /** A campaign that runs 11 to 15 October. */
    private static Campaign autumnSale() {
        return new Campaign("autumn-sale", oct(11), oct(15));
    }

    private static String window(Campaign c) {
        return c.getStartsOn() + ".." + c.getEndsOn();
    }

    @Test
    void movesAndResizesWithinTheSameWeek() {
        Campaign c = autumnSale();

        c.setEndsOn(oct(17));
        assertThat(window(c)).isEqualTo("2027-10-11..2027-10-17");

        c.moveTo(oct(12), oct(14));
        assertThat(window(c)).isEqualTo("2027-10-12..2027-10-14");

        c.moveTo(oct(13), oct(13));
        assertThat(window(c)).isEqualTo("2027-10-13..2027-10-13");
    }

    @Test
    void refusesAWindowThatEndsBeforeItStarts() {
        Campaign c = autumnSale();

        assertThatThrownBy(() -> c.moveTo(oct(28), oct(26))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> c.setEndsOn(oct(10))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void movesToALaterWeek() {
        Campaign c = autumnSale();

        c.moveTo(oct(25), oct(29));

        assertThat(window(c)).isEqualTo("2027-10-25..2027-10-29");
    }

    @Test
    void movesToAnEarlierWeek() {
        Campaign c = autumnSale();

        c.moveTo(oct(1), oct(4));

        assertThat(window(c)).isEqualTo("2027-10-01..2027-10-04");
    }

    @Test
    void aRefusedMoveLeavesTheCampaignWhereItWas() {
        Campaign c = autumnSale();

        assertThatThrownBy(() -> c.moveTo(oct(14), oct(12))).isInstanceOf(IllegalArgumentException.class);

        assertThat(window(c)).isEqualTo("2027-10-11..2027-10-15");
    }

    @Test
    void aRefusedLastDayLeavesTheOldOne() {
        Campaign c = autumnSale();

        assertThatThrownBy(() -> c.setEndsOn(oct(9))).isInstanceOf(IllegalArgumentException.class);

        assertThat(window(c)).isEqualTo("2027-10-11..2027-10-15");
    }
}
