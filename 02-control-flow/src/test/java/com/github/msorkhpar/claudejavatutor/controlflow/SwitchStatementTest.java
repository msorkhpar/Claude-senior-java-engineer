package com.github.msorkhpar.claudejavatutor.controlflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SwitchStatementTest {

    private SwitchStatement demo;

    @BeforeEach
    void setUp() {
        demo = new SwitchStatement();
    }

    @ParameterizedTest
    @CsvSource({
            "Monday, Weekday",
            "TUESDAY, Weekday",
            "wednesday, Weekday",
            "Thursday, Weekday",
            "Friday, Weekday",
            "Saturday, Weekend",
            "SUNDAY, Weekend"
    })
    void getDayType_shouldReturnCorrectDayType(String day, String expectedDayType) {
        assertThat(demo.getDayType(day)).isEqualTo(expectedDayType);
    }

    @Test
    void getDayType_shouldHandleInvalidDay() {
        assertThat(demo.getDayType("InvalidDay")).isEqualTo("Invalid day");
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2023, 31",
            "2, 2023, 28",
            "2, 2024, 29",
            "4, 2023, 30",
            "12, 2023, 31"
    })
    void getMonthDays_shouldReturnCorrectDays(int month, int year, int expectedDays) {
        assertThat(demo.getMonthDays(month, year)).isEqualTo(expectedDays);
    }

    @Test
    void getMonthDays_shouldHandleInvalidMonth() {
        assertThat(demo.getMonthDays(13, 2023)).isEqualTo(-1);
    }

    @ParameterizedTest
    @CsvSource({
            "1, Winter",
            "3, Spring",
            "7, Summer",
            "10, Fall",
            "12, Winter"
    })
    void getSeasonForMonth_shouldReturnCorrectSeason(int month, String expectedSeason) {
        assertThat(demo.getSeasonForMonth(month)).isEqualTo(expectedSeason);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 13, -1})
    void getSeasonForMonth_shouldHandleInvalidMonth(int invalidMonth) {
        assertThat(demo.getSeasonForMonth(invalidMonth)).isEqualTo("Invalid month");
    }
}