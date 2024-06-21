package com.github.msorkhpar.claudejavatutor.base;

import java.util.Objects;
import java.util.function.Supplier;

public class PerformanceTestUtil {

    public record MeasurementResult<T>(long executionTime, T result) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;

            // Check if o is an instance of the result type
            if (result != null && result.getClass().isInstance(o)) {
                return Objects.equals(result, o);
            }

            // Check if o is an instance of MeasurementResult
            if (!(o instanceof MeasurementResult<?> that)) return false;

            // Compare results
            if (result != null && result.getClass().isInstance(that.result)) {
                return result.equals(that.result);
            }

            return Objects.equals(result, that.result);
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
