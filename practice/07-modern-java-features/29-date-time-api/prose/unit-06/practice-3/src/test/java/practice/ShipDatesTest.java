package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The servers' default locale is set by each test: US English, except in the test of a server
 * whose image runs with a German default. That test runs first, so a formatter built once from
 * the default is built under German.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShipDatesTest {

    private Locale before;

    @BeforeEach
    void usEnglishServer() {
        before = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreTheDefault() {
        Locale.setDefault(before);
    }

    @Test
    @Order(1)
    void aServerWithAGermanDefaultReadsAndWritesTheSame() {
        Locale.setDefault(Locale.GERMANY);
        assertThat(ShipDates.shipped("05-Dec-2031")).isEqualTo(LocalDate.of(2031, 12, 5));
        assertThat(ShipDates.receiptDate(LocalDate.of(2032, 3, 19))).isEqualTo("Friday 19 March 2032");
    }

    @Test
    @Order(2)
    void readsTheCarriersCells() {
        assertThat(ShipDates.shipped("14-Nov-2031")).isEqualTo(LocalDate.of(2031, 11, 14));
        assertThat(ShipDates.shipped("02-Mar-2032")).isEqualTo(LocalDate.of(2032, 3, 2));
        assertThat(ShipDates.shipped("30-Jun-2031")).isEqualTo(LocalDate.of(2031, 6, 30));
    }

    @Test
    @Order(3)
    void writesAReceiptLineWithTheDate() {
        assertThat(ShipDates.receiptFor("14-Nov-2031")).startsWith("Shipped ").contains("14").endsWith("2031");
    }

    @Test
    @Order(4)
    void readsSeptemberAsTheCarrierWritesIt() {
        assertThat(ShipDates.shipped("09-Sep-2031")).isEqualTo(LocalDate.of(2031, 9, 9));
    }

    @Test
    @Order(5)
    void readsCellsInCapitals() {
        assertThat(ShipDates.shipped("21-OCT-2031")).isEqualTo(LocalDate.of(2031, 10, 21));
        assertThat(ShipDates.shipped("01-JAN-2032")).isEqualTo(LocalDate.of(2032, 1, 1));
    }

    @Test
    @Order(6)
    void theReceiptSpellsOutTheDayAndMonth() {
        assertThat(ShipDates.receiptDate(LocalDate.of(2031, 11, 14))).isEqualTo("Friday 14 November 2031");
        assertThat(ShipDates.receiptDate(LocalDate.of(2032, 7, 6))).isEqualTo("Tuesday 6 July 2032");
    }
}
