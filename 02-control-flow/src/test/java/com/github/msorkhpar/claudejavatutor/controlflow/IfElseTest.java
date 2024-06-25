package com.github.msorkhpar.claudejavatutor.controlflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IfElseTest {

    @Test
    void testGetLetterGrade() {
        assertEquals("A", IfElse.getLetterGrade(95));
        assertEquals("B", IfElse.getLetterGrade(85));
        assertEquals("C", IfElse.getLetterGrade(75));
        assertEquals("D", IfElse.getLetterGrade(65));
        assertEquals("F", IfElse.getLetterGrade(55));
    }

    @Test
    void testIsEvenPositive() {
        assertTrue(IfElse.isEvenPositive(2));
        assertFalse(IfElse.isEvenPositive(3));
        assertFalse(IfElse.isEvenPositive(-2));
        assertFalse(IfElse.isEvenPositive(0));
    }

    @Test
    void testGetAgeCategory() {
        assertEquals("Invalid", IfElse.getAgeCategory(-1));
        assertEquals("Minor", IfElse.getAgeCategory(17));
        assertEquals("Adult", IfElse.getAgeCategory(18));
        assertEquals("Adult", IfElse.getAgeCategory(64));
        assertEquals("Senior", IfElse.getAgeCategory(65));
    }

    @Test
    void testFindMax() {
        assertEquals(5, IfElse.findMax(5, 3, 1));
        assertEquals(5, IfElse.findMax(1, 5, 3));
        assertEquals(5, IfElse.findMax(3, 1, 5));
        assertEquals(5, IfElse.findMax(5, 5, 5));
    }

    @Test
    void testGetDayType() {
        assertEquals("Weekend", IfElse.getDayType("Saturday"));
        assertEquals("Weekend", IfElse.getDayType("sunday"));
        assertEquals("Weekday", IfElse.getDayType("Monday"));
        assertEquals("Weekday", IfElse.getDayType("friday"));
        assertEquals("Invalid day", IfElse.getDayType("Someday"));
        assertEquals("Invalid input", IfElse.getDayType(null));
    }
}