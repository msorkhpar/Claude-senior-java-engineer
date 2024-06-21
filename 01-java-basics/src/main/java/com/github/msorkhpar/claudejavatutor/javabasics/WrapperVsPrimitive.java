package com.github.msorkhpar.claudejavatutor.javabasics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class WrapperVsPrimitive {

    public long sumPrimitives(int limit) {
        long sum = 0;
        for (int i = 0; i < limit; i++) {
            sum += i;
        }
        return sum;
    }

    public Long sumWrappers(int limit) {
        Long sum = 0L;
        for (Integer i = 0; i < limit; i++) {
            sum += i;
        }
        return sum;
    }

    public long sumPrimitiveArray(int[] array) {
        long sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    public long sumWrapperArray(Integer[] array) {
        long sum = 0;
        for (Integer value : array) {
            if (value != null) {
                sum += value;
            }
        }
        return sum;
    }

    public long sumPrimitiveStream(int limit) {
        return LongStream.range(0, limit).sum();
    }

    public long sumWrapperStream(int limit) {
        return Stream.iterate(0, i -> i < limit, i -> i + 1)
                .mapToLong(Long::valueOf)
                .sum();
    }


    public Integer getNullInteger() {
        return null;
    }

    public Integer getNonNullInteger() {
        return 42;
    }

    public List<Integer> getListOfIntegers(int count) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            numbers.add(i);
        }
        return numbers;
    }
}