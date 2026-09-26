package practice;

import org.junit.jupiter.api.Test;

import practice.Results.Result;
import practice.Results.ResultStatus;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ResultsTest {

    @Test
    void mapsAndChainsASuccess() {
        Result<Integer> doubled = Results.success("42").map(Integer::parseInt).map(n -> n * 2);
        assertThat(doubled.status()).isEqualTo(ResultStatus.SUCCESS);
        assertThat(doubled.getValue()).contains(84);
        Result<Integer> chained = Results.success("7").flatMap(s -> Results.success(Integer.parseInt(s) + 1));
        assertThat(chained.getValue()).contains(8);
        assertThat(Results.<String>failure("gone").getValue()).isEmpty();
    }

    @Test
    void aFailureNeverCallsTheMapper() {
        AtomicInteger calls = new AtomicInteger();
        Result<Integer> mapped = Results.<String>failure("not found").map(s -> {
            calls.incrementAndGet();
            return s.length();
        });
        Result<Integer> flat = Results.<String>failure("not found").flatMap(s -> {
            calls.incrementAndGet();
            return Results.success(s.length());
        });
        assertThat(calls.get()).as("mapper calls").isZero();
        assertThat(mapped.status()).isEqualTo(ResultStatus.FAILURE);
        assertThat(mapped.message()).isEqualTo("not found");
        assertThat(flat.status()).isEqualTo(ResultStatus.FAILURE);
        assertThat(flat.getValue()).isEmpty();
        Result<Integer> carried = new Result<String>(ResultStatus.FAILURE, "left over", "bad").map(String::length);
        assertThat(carried.value()).isNull();
        assertThat(carried.getValue()).isEmpty();
    }

    @Test
    void aPendingResultStaysPending() {
        Result<Integer> mapped = Results.<String>pending().map(String::length);
        assertThat(mapped.status()).isEqualTo(ResultStatus.PENDING);
        assertThat(mapped.getValue()).isEmpty();
        AtomicInteger calls = new AtomicInteger();
        Result<Integer> flat = Results.<String>pending().flatMap(s -> {
            calls.incrementAndGet();
            return Results.success(s.length());
        });
        assertThat(flat.status()).isEqualTo(ResultStatus.PENDING);
        assertThat(calls.get()).isZero();
    }

    @Test
    void flatMapKeepsTheInnerFailure() {
        Result<Integer> result = Results.success("x").flatMap(s -> Results.<Integer>failure("bad input"));
        assertThat(result.status()).isEqualTo(ResultStatus.FAILURE);
        assertThat(result.message()).isEqualTo("bad input");
    }
}
