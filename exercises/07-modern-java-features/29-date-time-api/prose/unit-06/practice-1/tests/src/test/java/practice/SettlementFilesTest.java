package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementFilesTest {

    @Test
    void namesAMorningRunAfterItsDateAndTime() {
        assertThat(SettlementFiles.fileName(LocalDateTime.of(2031, 4, 8, 9, 4)))
                .isEqualTo("payments_20310408_0904.txt");
    }

    @Test
    void leavesSecondsAndFractionsOut() {
        assertThat(SettlementFiles.fileName(LocalDateTime.of(2031, 7, 15, 11, 7, 59, 500_000_000)))
                .isEqualTo("payments_20310715_1107.txt");
    }

    @Test
    void aRunInTheLastDaysOfDecemberCarriesThatYear() {
        assertThat(SettlementFiles.fileName(LocalDateTime.of(2031, 12, 30, 9, 12)))
                .isEqualTo("payments_20311230_0912.txt");
        assertThat(SettlementFiles.fileName(LocalDateTime.of(2032, 12, 27, 8, 12)))
                .isEqualTo("payments_20321227_0812.txt");
    }

    @Test
    void anAfternoonRunIsNotNamedLikeANightRun() {
        assertThat(SettlementFiles.fileName(LocalDateTime.of(2031, 6, 2, 16, 6)))
                .isEqualTo("payments_20310602_1606.txt");
    }

    @Test
    void theMonthAndTheMinuteStayInTheirPlaces() {
        assertThat(SettlementFiles.fileName(LocalDateTime.of(2031, 3, 11, 10, 45)))
                .isEqualTo("payments_20310311_1045.txt");
    }
}
