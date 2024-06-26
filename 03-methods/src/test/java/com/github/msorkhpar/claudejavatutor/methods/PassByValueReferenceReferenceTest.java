package com.github.msorkhpar.claudejavatutor.methods;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PassByValueReferenceReferenceTest {

    @Test
    void testModifyPrimitive() {
        int x = 10;
        PassByValueReference.modifyPrimitive(x);
        assertThat(x).isEqualTo(10);
    }

    @Test
    void testModifyObject() {
        StringBuilder sb = new StringBuilder("Hello");
        PassByValueReference.modifyObject(sb);
        assertThat(sb.toString()).isEqualTo("Hello World");
    }

    @Test
    void testReassignObject() {
        StringBuilder sb = new StringBuilder("Hello");
        PassByValueReference.reassignObject(sb);
        assertThat(sb.toString()).isEqualTo("Hello");
    }

    @Test
    void testModifyArray() {
        int[] arr = {1, 2, 3};
        PassByValueReference.modifyArray(arr);
        assertThat(arr[0]).isEqualTo(100);
    }

    @Test
    void testReassignArray() {
        int[] arr = {1, 2, 3};
        PassByValueReference.reassignArray(arr);
        assertThat(arr).containsExactly(1, 2, 3);
    }

    @Test
    void testStringBehavior() {
        String s = "Hello";
        modifyString(s);
        assertThat(s).isEqualTo("Hello");
    }

    private void modifyString(String s) {
        s += " World";
    }

    @Test
    void testWrapperClassBehavior() {
        Integer i = 10;
        modifyInteger(i);
        assertThat(i).isEqualTo(10);
    }

    @Test
    void testReassigningReference() {
        StringBuilder sb = new StringBuilder("Original");
        PassByValueReference.reassignReference(sb);
        assertThat(sb.toString()).isEqualTo("Original");  // Original object is unchanged
    }

    private void modifyInteger(Integer i) {
        i = 20;
    }
}