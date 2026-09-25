package com.github.msorkhpar.claudejavatutor.base;

import java.util.Objects;
import java.util.function.Supplier;

public class PerformanceTestUtil {

    public record MeasurementResult<T>(long executionTime, T result) {

        /**
         * Two measurements are equal when their results are equal; the execution time is ignored,
         * because two runs of the same operation almost never take the same number of nanoseconds.
         * A measurement is never equal to a bare result value (that would break symmetry:
         * {@code 42L.equals(measurement)} is always false), so compare {@link #result()} explicitly.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MeasurementResult<?> that)) return false;
            return Objects.equals(result, that.result);
        }

        // equals() above compares only the result, so hashCode() must hash only the result too;
        // the record's generated hashCode() would also hash executionTime and break the contract.
        @Override
        public int hashCode() {
            return Objects.hashCode(result);
        }

        @Override
        public String toString() {
            return "MeasurementResult{" +
                    "executionTime=" + executionTime +
                    ", result=" + result +
                    '}';
        }
    }

    public static <T> MeasurementResult<T> measureExecution(Supplier<T> operation) {
        long startTime = System.nanoTime();
        T result = operation.get();
        long endTime = System.nanoTime();
        return new MeasurementResult<>(endTime - startTime, result);
    }
}
