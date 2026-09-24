package com.github.msorkhpar.claudejavatutor.datetimeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Legacy Date Limitations Tests")
class LegacyDateLimitationsTest {

    @Nested
    @DisplayName("Mutability Issues")
    class MutabilityTests {

        @Test
        @DisplayName("Should demonstrate that Date is mutable")
        @SuppressWarnings("deprecation")
        void testDateMutability() {
            Date date = LegacyDateLimitations.createAndMutateDate();

            // Date was created as 2024 but mutated to 2025
            assertThat(date.getYear() + 1900).isEqualTo(2025);
        }
    }

    @Nested
    @DisplayName("Year Offset Confusion")
    class YearOffsetTests {

        @Test
        @DisplayName("Should require adding 1900 to get actual year")
        @SuppressWarnings("deprecation")
        void testYearOffset() {
            Date date = new Date(124, Calendar.MARCH, 15); // 2024
            int actualYear = LegacyDateLimitations.getYearFromLegacyDate(date);
            assertThat(actualYear).isEqualTo(2024);
        }

        @Test
        @DisplayName("Should handle epoch year correctly")
        @SuppressWarnings("deprecation")
        void testEpochYear() {
            Date date = new Date(70, Calendar.JANUARY, 1); // 1970
            int actualYear = LegacyDateLimitations.getYearFromLegacyDate(date);
            assertThat(actualYear).isEqualTo(1970);
        }
    }

    @Nested
    @DisplayName("Zero-Based Month Issues")
    class MonthIndexTests {

        @Test
        @DisplayName("Should demonstrate zero-based month indexing in Calendar")
        void testZeroBasedMonth() {
            // Passing March (3) should result in Calendar.MARCH (2)
            int calendarMonth = LegacyDateLimitations.getMonthFromCalendar(2024, 3, 15);
            assertThat(calendarMonth).isEqualTo(Calendar.MARCH); // 2
        }

        @Test
        @DisplayName("Should demonstrate January as month 0")
        void testJanuaryIsZero() {
            int calendarMonth = LegacyDateLimitations.getMonthFromCalendar(2024, 1, 1);
            assertThat(calendarMonth).isEqualTo(0); // January = 0
        }

        @Test
        @DisplayName("Should demonstrate December as month 11")
        void testDecemberIsEleven() {
            int calendarMonth = LegacyDateLimitations.getMonthFromCalendar(2024, 12, 31);
            assertThat(calendarMonth).isEqualTo(11); // December = 11
        }
    }

    @Nested
    @DisplayName("Thread Safety Issues")
    class ThreadSafetyTests {

        @Test
        @DisplayName("A SimpleDateFormat used by one thread gives correct results")
        void testSimpleDateFormatSingleThread() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date(1_710_460_800_000L); // 2024-03-15T00:00:00Z

            assertThat(LegacyDateLimitations.formatDateUnsafe(date, sdf)).isEqualTo("2024-03-15");
        }

        @Test
        @Timeout(value = 60, unit = TimeUnit.SECONDS)
        @DisplayName("One SimpleDateFormat shared by 8 threads produces corrupted parses")
        void testSharedSimpleDateFormatCorrupts() {
            int wrong = LegacyDateLimitations.wrongParsesWithSharedSimpleDateFormat(8, 5_000, 200);

            assertThat(wrong).isPositive();
        }

        @Test
        @Timeout(value = 60, unit = TimeUnit.SECONDS)
        @DisplayName("One DateTimeFormatter shared by 8 threads never produces a wrong parse")
        void testSharedDateTimeFormatterIsSafe() {
            int wrong = LegacyDateLimitations.wrongParsesWithSharedDateTimeFormatter(8, 5_000, 5);

            assertThat(wrong).isZero();
        }
    }

    @Nested
    @DisplayName("Lenient Mode Issues")
    class LenientModeTests {

        @Test
        @DisplayName("Should silently accept invalid date in lenient mode")
        void testLenientMode() {
            // February 30 doesn't exist but lenient mode wraps it
            Date date = LegacyDateLimitations.createLenientDate(2024, 2, 30);
            assertThat(date).isNotNull();
            // Should have rolled over to March
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.MARCH);
        }

        @Test
        @DisplayName("Should throw when strict mode rejects invalid date")
        void testStrictMode() {
            assertThatThrownBy(() ->
                    LegacyDateLimitations.createStrictDate(2024, 2, 30)
            ).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should accept valid date in strict mode")
        void testStrictModeValidDate() {
            Date date = LegacyDateLimitations.createStrictDate(2024, 2, 29); // Leap year
            assertThat(date).isNotNull();
        }
    }

    @Nested
    @DisplayName("Date Includes Time")
    class DateIncludesTimeTests {

        @Test
        @DisplayName("A Date is one instant: the day and time you see depend on the zone")
        void testDateIsAnInstant() {
            Date date = new Date(1_710_460_800_000L); // 2024-03-15T00:00:00Z

            assertThat(date.getTime()).isEqualTo(1_710_460_800_000L);
            assertThat(LegacyDateLimitations.dateAndTimeIn(date, TimeZone.getTimeZone("UTC")))
                    .isEqualTo("2024-03-15 00:00");
            assertThat(LegacyDateLimitations.dateAndTimeIn(date, TimeZone.getTimeZone("America/New_York")))
                    .isEqualTo("2024-03-14 20:00"); // a different calendar day
            assertThat(LegacyDateLimitations.dateAndTimeIn(date, TimeZone.getTimeZone("Asia/Tokyo")))
                    .isEqualTo("2024-03-15 09:00");
        }

        @Test
        @SuppressWarnings("deprecation")
        @DisplayName("Even a 'date-only' Date carries a time: midnight in the JVM default zone")
        void testDateOnlyConstructorStillCarriesTime() {
            TimeZone original = TimeZone.getDefault();
            try {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
                Date dateOnly = new Date(124, Calendar.MARCH, 15); // "2024-03-15"

                assertThat(LegacyDateLimitations.dateAndTimeIn(dateOnly, TimeZone.getTimeZone("Asia/Tokyo")))
                        .isEqualTo("2024-03-15 00:00");
                assertThat(LegacyDateLimitations.dateAndTimeIn(dateOnly, TimeZone.getTimeZone("UTC")))
                        .isEqualTo("2024-03-14 15:00");
            } finally {
                TimeZone.setDefault(original);
            }
        }
    }

    @Nested
    @DisplayName("SQL Date Conversion")
    class SqlDateTests {

        @Test
        @DisplayName("Should convert util.Date to sql.Date")
        void testConvertToSqlDate() {
            Date utilDate = new Date();
            java.sql.Date sqlDate = LegacyDateLimitations.convertToSqlDate(utilDate);
            assertThat(sqlDate.getTime()).isEqualTo(utilDate.getTime());
        }
    }

    @Nested
    @DisplayName("Parsing Issues")
    class ParsingTests {

        @Test
        @DisplayName("Should parse valid date string")
        void testParseValid() throws ParseException {
            Date date = LegacyDateLimitations.parseDateString("2024-03-15", "yyyy-MM-dd");
            assertThat(date).isNotNull();
        }

        @Test
        @DisplayName("Should throw on invalid date string in strict mode")
        void testParseInvalid() {
            assertThatThrownBy(() ->
                    LegacyDateLimitations.parseDateString("2024-13-01", "yyyy-MM-dd")
            ).isInstanceOf(ParseException.class);
        }

        @Test
        @DisplayName("Should throw on mismatched pattern")
        void testParseMismatchedPattern() {
            assertThatThrownBy(() ->
                    LegacyDateLimitations.parseDateString("15/03/2024", "yyyy-MM-dd")
            ).isInstanceOf(ParseException.class);
        }
    }

    @Nested
    @DisplayName("Calendar Overflow")
    class CalendarOverflowTests {

        @Test
        @DisplayName("Should demonstrate calendar month overflow in lenient mode")
        void testOverflow() {
            Date date = LegacyDateLimitations.overflowingCalendar();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            // Month 12 (0-indexed) = January of next year
            assertThat(cal.get(Calendar.YEAR)).isEqualTo(2025);
            assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
        }
    }
}
