package com.github.msorkhpar.claudejavatutor.controlflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SwitchExpressionTest {

    private SwitchExpression demo;

    @BeforeEach
    void setUp() {
        demo = new SwitchExpression();
    }

    @ParameterizedTest
    @EnumSource(value = SwitchExpression.Day.class, names = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"})
    void getDayType_shouldReturnWeekdayForWeekdays(SwitchExpression.Day day) {
        assertThat(demo.getDayType(day)).isEqualTo("Weekday");
    }

    @ParameterizedTest
    @EnumSource(value = SwitchExpression.Day.class, names = {"SATURDAY", "SUNDAY"})
    void getDayType_shouldReturnWeekendForWeekends(SwitchExpression.Day day) {
        assertThat(demo.getDayType(day)).isEqualTo("Weekend");
    }

    @Test
    void getActivity_shouldReturnCorrectActivityForEachDay() {
        assertThat(demo.getActivity(SwitchExpression.Day.MONDAY)).isEqualTo("Relax");
        assertThat(demo.getActivity(SwitchExpression.Day.TUESDAY)).isEqualTo("Work");
        assertThat(demo.getActivity(SwitchExpression.Day.WEDNESDAY)).isEqualTo("Study");
        assertThat(demo.getActivity(SwitchExpression.Day.THURSDAY)).isEqualTo("Party");
        assertThat(demo.getActivity(SwitchExpression.Day.FRIDAY)).isEqualTo("Relax");
        assertThat(demo.getActivity(SwitchExpression.Day.SATURDAY)).isEqualTo("Party");
        assertThat(demo.getActivity(SwitchExpression.Day.SUNDAY)).isEqualTo("Relax");
    }

    @ParameterizedTest
    @ValueSource(strings = {"success", "SUCCESS", "Success"})
    void getComplexStatus_shouldReturn1ForSuccess(String status) {
        assertThat(demo.getComplexStatus(status)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"error", "ERROR", "Error"})
    void getComplexStatus_shouldReturnNegative1ForError(String status) {
        assertThat(demo.getComplexStatus(status)).isEqualTo(-1);
    }

    @Test
    void getComplexStatus_shouldReturn0ForUnknownStatus() {
        assertThat(demo.getComplexStatus("unknown")).isEqualTo(0);
    }

    @Test
    void handleObject_shouldHandleDifferentTypes() {
        assertThat(demo.handleObject("test")).isEqualTo("String: test");
        assertThat(demo.handleObject(42)).isEqualTo("Integer: 42");
        assertThat(demo.handleObject(SwitchExpression.Day.MONDAY)).isEqualTo("Day: MONDAY");
        assertThat(demo.handleObject(new Object())).isEqualTo("Unknown type");
    }

    @Test
    void handleObject_shouldHandleNullInput() {
        assertThat(demo.handleObject(null)).isEqualTo("Null input");
    }
}