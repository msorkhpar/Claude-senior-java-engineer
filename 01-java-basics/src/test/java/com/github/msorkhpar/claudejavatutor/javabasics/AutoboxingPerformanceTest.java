package com.github.msorkhpar.claudejavatutor.javabasics;

import com.github.msorkhpar.claudejavatutor.base.PerformanceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

class AutoboxingPerformanceTest {

    private WrapperVsPrimitive testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new WrapperVsPrimitive();
    }


    @Test
    void testAutoboxingVsPrimitive() {
        int limit = 10_000_000;
        long expectedSum = (long) limit * (limit - 1) / 2;

        var primitiveResult = PerformanceTestUtil.measureExecution(() -> testSubject.sumPrimitives(limit));
        var wrapperResult = PerformanceTestUtil.measureExecution(() -> testSubject.sumWrappers(limit));

        System.out.println("Primitive sum time: " + primitiveResult.executionTime() + "ns");
        System.out.println("Wrapper sum time: " + wrapperResult.executionTime() + "ns");

        assertThat(primitiveResult.result()).isEqualTo(expectedSum);
        assertThat(wrapperResult.result()).isEqualTo(expectedSum);
        assertThat(wrapperResult.executionTime()).isGreaterThan(primitiveResult.executionTime());
    }

    @Test
    void testPrimitiveVsWrapperArrayPerformance() {
        int size = 10_000_000;
        int[] primitiveArray = new int[size];
        Integer[] wrapperArray = new Integer[size];
        for (int i = 0; i < size; i++) {
            primitiveArray[i] = i;
            wrapperArray[i] = i;
        }

        var primitiveSum = PerformanceTestUtil.measureExecution(() ->  testSubject.sumPrimitiveArray(primitiveArray));
        var wrapperSum = PerformanceTestUtil.measureExecution(() ->  testSubject.sumWrapperArray(wrapperArray));

        System.out.println("Primitive array sum time: " + primitiveSum.executionTime() + "ns");
        System.out.println("Wrapper array sum time: " + wrapperSum.executionTime() + "ns");

        assertThat(primitiveSum.executionTime()).isLessThan(wrapperSum.executionTime());
        assertThat(primitiveSum).isEqualTo(wrapperSum);
    }

    @Test
    void testPrimitiveStreamVsWrapperStream() {
        int limit = 10_000_000;

        var primitiveStreamSum = PerformanceTestUtil.measureExecution(() ->  testSubject.sumPrimitiveStream(limit));
        var wrapperStreamSum = PerformanceTestUtil.measureExecution(() ->  testSubject.sumWrapperStream(limit));

        System.out.println("Primitive stream sum time: " + primitiveStreamSum.executionTime() + "ns");
        System.out.println("Wrapper stream sum time: " + wrapperStreamSum.executionTime() + "ns");

        assertThat(primitiveStreamSum).isEqualTo(wrapperStreamSum);
        assertThat(wrapperStreamSum.executionTime()).isGreaterThan(primitiveStreamSum.executionTime());
    }

    @Test
    void testWrapperArrayWithNulls() {

        Integer[] arrayWithNulls = new Integer[]{1, null, 3, 4, null};
        long sum = testSubject.sumWrapperArray(arrayWithNulls);
        long expectedSum = IntStream.of(1, 3, 4).sum();

        assertThat(sum).isEqualTo(expectedSum);
        System.out.println("Sum of array with nulls: " + sum);
    }

    @Test
    void testNullabilityOfWrappers() {
        Integer nullableInteger = testSubject.getNullInteger();
        assertThat(nullableInteger).isNull();

        Integer nonNullInteger = testSubject.getNonNullInteger();
        assertThat(nonNullInteger).isNotNull();
    }

    @Test
    void testGenericUsageOfWrappers() {
        List<Integer> numbers = testSubject.getListOfIntegers(5);
        assertThat(numbers).hasSize(5).containsExactly(1, 2, 3, 4, 5);
    }
}