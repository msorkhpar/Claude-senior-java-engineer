package com.github.msorkhpar.claudejavatutor.datetimeapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LocalDate, LocalTime, and LocalDateTime Tests")
class LocalDateTimeExamplesTest {

    @Nested
    @DisplayName("LocalDate Operations")
    class LocalDateTests {

        @Test
        @DisplayName("Should create a date from components")
        void testCreateDate() {
            LocalDate date = LocalDateTimeExamples.createDate(2024, 3, 15);
            assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        }

        @Test
        @DisplayName("Should throw for invalid date")
        void testInvalidDate() {
            assertThatThrownBy(() -> LocalDateTimeExamples.createDate(2024, 2, 30))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("Should throw for invalid month")
        void testInvalidMonth() {
            assertThatThrownBy(() -> LocalDateTimeExamples.createDate(2024, 13, 1))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("Should calculate age correctly")
        void testCalculateAge() {
            LocalDate birth = LocalDate.of(1990, 6, 15);
            LocalDate reference = LocalDate.of(2024, 3, 15);
            assertThat(LocalDateTimeExamples.calculateAge(birth, reference)).isEqualTo(33);
        }

        @Test
        @DisplayName("Should calculate age as zero for same date")
        void testCalculateAgeZero() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            assertThat(LocalDateTimeExamples.calculateAge(date, date)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw for null birth date")
        void testCalculateAgeNullBirth() {
            assertThatThrownBy(() -> LocalDateTimeExamples.calculateAge(null, LocalDate.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw for birth date after reference")
        void testCalculateAgeFutureBirth() {
            LocalDate future = LocalDate.of(2030, 1, 1);
            LocalDate now = LocalDate.of(2024, 1, 1);
            assertThatThrownBy(() -> LocalDateTimeExamples.calculateAge(future, now))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should get date range")
        void testGetDateRange() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 4);
            List<LocalDate> range = LocalDateTimeExamples.getDateRange(start, end);
            assertThat(range).containsExactly(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 3)
            );
        }

        @Test
        @DisplayName("Should return empty range for same start and end")
        void testGetDateRangeSameDay() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            List<LocalDate> range = LocalDateTimeExamples.getDateRange(date, date);
            assertThat(range).isEmpty();
        }

        @Test
        @DisplayName("Should throw for reversed range")
        void testGetDateRangeReversed() {
            LocalDate start = LocalDate.of(2024, 1, 5);
            LocalDate end = LocalDate.of(2024, 1, 1);
            assertThatThrownBy(() -> LocalDateTimeExamples.getDateRange(start, end))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should identify leap years correctly")
        void testIsLeapYear() {
            assertThat(LocalDateTimeExamples.isLeapYear(2024)).isTrue();
            assertThat(LocalDateTimeExamples.isLeapYear(2023)).isFalse();
            assertThat(LocalDateTimeExamples.isLeapYear(2000)).isTrue();
            assertThat(LocalDateTimeExamples.isLeapYear(1900)).isFalse();
        }

        @Test
        @DisplayName("Should get last day of month")
        void testGetLastDayOfMonth() {
            assertThat(LocalDateTimeExamples.getLastDayOfMonth(LocalDate.of(2024, 2, 1)))
                    .isEqualTo(LocalDate.of(2024, 2, 29)); // Leap year
            assertThat(LocalDateTimeExamples.getLastDayOfMonth(LocalDate.of(2023, 2, 1)))
                    .isEqualTo(LocalDate.of(2023, 2, 28)); // Non-leap year
            assertThat(LocalDateTimeExamples.getLastDayOfMonth(LocalDate.of(2024, 12, 5)))
                    .isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("Should get next day of week")
        void testGetNextDayOfWeek() {
            // 2024-01-01 is Monday
            LocalDate monday = LocalDate.of(2024, 1, 1);
            assertThat(LocalDateTimeExamples.getNextDayOfWeek(monday, DayOfWeek.FRIDAY))
                    .isEqualTo(LocalDate.of(2024, 1, 5));
            assertThat(LocalDateTimeExamples.getNextDayOfWeek(monday, DayOfWeek.MONDAY))
                    .isEqualTo(LocalDate.of(2024, 1, 8)); // Next Monday, not same day
        }

        @Test
        @DisplayName("Should count business days")
        void testCountBusinessDays() {
            // Monday to Friday = 5 business days
            LocalDate monday = LocalDate.of(2024, 1, 1);
            LocalDate nextMonday = LocalDate.of(2024, 1, 8);
            assertThat(LocalDateTimeExamples.countBusinessDays(monday, nextMonday)).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return zero business days for same day")
        void testCountBusinessDaysSameDay() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            assertThat(LocalDateTimeExamples.countBusinessDays(date, date)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should get quarter correctly")
        void testGetQuarter() {
            assertThat(LocalDateTimeExamples.getQuarter(LocalDate.of(2024, 1, 1))).isEqualTo(1);
            assertThat(LocalDateTimeExamples.getQuarter(LocalDate.of(2024, 4, 1))).isEqualTo(2);
            assertThat(LocalDateTimeExamples.getQuarter(LocalDate.of(2024, 7, 1))).isEqualTo(3);
            assertThat(LocalDateTimeExamples.getQuarter(LocalDate.of(2024, 12, 31))).isEqualTo(4);
        }

        @Test
        @DisplayName("Should get day of year")
        void testGetDayOfYear() {
            assertThat(LocalDateTimeExamples.getDayOfYear(LocalDate.of(2024, 1, 1))).isEqualTo(1);
            assertThat(LocalDateTimeExamples.getDayOfYear(LocalDate.of(2024, 12, 31))).isEqualTo(366); // Leap year
        }
    }

    @Nested
    @DisplayName("LocalTime Operations")
    class LocalTimeTests {

        @Test
        @DisplayName("Should create time from components")
        void testCreateTime() {
            LocalTime time = LocalDateTimeExamples.createTime(14, 30, 45);
            assertThat(time).isEqualTo(LocalTime.of(14, 30, 45));
        }

        @Test
        @DisplayName("Should throw for invalid time")
        void testInvalidTime() {
            assertThatThrownBy(() -> LocalDateTimeExamples.createTime(25, 0, 0))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("Should calculate minutes between times")
        void testMinutesBetween() {
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(17, 30);
            assertThat(LocalDateTimeExamples.minutesBetween(start, end)).isEqualTo(510);
        }

        @Test
        @DisplayName("Should return negative minutes when end is before start")
        void testMinutesBetweenNegative() {
            LocalTime start = LocalTime.of(17, 0);
            LocalTime end = LocalTime.of(9, 0);
            assertThat(LocalDateTimeExamples.minutesBetween(start, end)).isNegative();
        }

        @Test
        @DisplayName("Should check time within normal range")
        void testIsWithinRange() {
            LocalTime time = LocalTime.of(12, 0);
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(17, 0);
            assertThat(LocalDateTimeExamples.isWithinRange(time, start, end)).isTrue();
        }

        @Test
        @DisplayName("Should check time outside range")
        void testIsOutsideRange() {
            LocalTime time = LocalTime.of(20, 0);
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(17, 0);
            assertThat(LocalDateTimeExamples.isWithinRange(time, start, end)).isFalse();
        }

        @Test
        @DisplayName("Should check time within overnight range")
        void testIsWithinOvernightRange() {
            LocalTime time = LocalTime.of(23, 0);
            LocalTime start = LocalTime.of(22, 0);
            LocalTime end = LocalTime.of(6, 0);
            assertThat(LocalDateTimeExamples.isWithinRange(time, start, end)).isTrue();
        }

        @Test
        @DisplayName("Should check time at boundaries")
        void testIsWithinRangeBoundary() {
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(17, 0);
            assertThat(LocalDateTimeExamples.isWithinRange(start, start, end)).isTrue();
            assertThat(LocalDateTimeExamples.isWithinRange(end, start, end)).isTrue();
        }

        @Test
        @DisplayName("Should truncate time to hour")
        void testTruncateToHour() {
            LocalTime time = LocalTime.of(14, 45, 30, 123456789);
            assertThat(LocalDateTimeExamples.truncateToHour(time)).isEqualTo(LocalTime.of(14, 0));
        }
    }

    @Nested
    @DisplayName("LocalDateTime Operations")
    class LocalDateTimeTests {

        @Test
        @DisplayName("Should combine date and time")
        void testCombine() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            LocalTime time = LocalTime.of(14, 30);
            LocalDateTime result = LocalDateTimeExamples.combine(date, time);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 3, 15, 14, 30));
        }

        @Test
        @DisplayName("Should add business days correctly")
        void testAddBusinessDays() {
            // Friday 2024-01-05
            LocalDateTime friday = LocalDateTime.of(2024, 1, 5, 9, 0);
            // Adding 1 business day should skip weekend to Monday
            LocalDateTime result = LocalDateTimeExamples.addBusinessDays(friday, 1);
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 1, 8));
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.of(9, 0));
        }

        @Test
        @DisplayName("Should identify weekend correctly")
        void testIsWeekend() {
            LocalDateTime saturday = LocalDateTime.of(2024, 1, 6, 12, 0);
            LocalDateTime sunday = LocalDateTime.of(2024, 1, 7, 12, 0);
            LocalDateTime monday = LocalDateTime.of(2024, 1, 8, 12, 0);

            assertThat(LocalDateTimeExamples.isWeekend(saturday)).isTrue();
            assertThat(LocalDateTimeExamples.isWeekend(sunday)).isTrue();
            assertThat(LocalDateTimeExamples.isWeekend(monday)).isFalse();
        }
    }
}
