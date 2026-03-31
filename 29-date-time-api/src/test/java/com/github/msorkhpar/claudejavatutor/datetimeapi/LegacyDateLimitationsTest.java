package com.github.msorkhpar.claudejavatutor.datetimeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
        @DisplayName("Should demonstrate SimpleDateFormat thread unsafety potential")
        void testSimpleDateFormatThreadUnsafe() throws InterruptedException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String expected = sdf.format(date);

            // Single-threaded usage should work fine
            String result = LegacyDateLimitations.formatDateUnsafe(date, sdf);
            assertThat(result).isEqualTo(expected);
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
        @DisplayName("Should demonstrate that Date carries time information")
        void testDateIncludesTime() {
            boolean result = LegacyDateLimitations.dateIncludesTime();
            assertThat(result).isTrue();
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
