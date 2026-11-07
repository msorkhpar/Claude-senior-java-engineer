package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FieldsTest {

    private static final Duration LIMIT = Duration.ofSeconds(10);
    private static final String ORDER = "sku=K-204;qty=12;wrap;note=ring twice";

    @Test
    void readsTheFirstField() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Fields.valueOf(ORDER, "sku")).contains("K-204");
            assertThat(Fields.valueOf("lot=77;bin=4", "lot")).contains("77");
        });
    }

    @Test
    void readsTheLastFieldAndReportsAMissingOne() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Fields.valueOf(ORDER, "note")).contains("ring twice");
            assertThat(Fields.valueOf(ORDER, "coupon")).isEqualTo(Optional.empty());
            assertThat(Fields.valueOf("lot=77", "lot")).contains("77");
        });
    }

    @Test
    void readsAFieldInTheMiddle() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Fields.valueOf(ORDER, "qty")).contains("12");
            assertThat(Fields.valueOf("a=1;bb=22;c=3", "bb")).contains("22");
        });
    }

    @Test
    void aFlagHasAnEmptyValue() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Fields.valueOf("express;sku=M-9", "express")).contains("");
            assertThat(Fields.valueOf("express;sku=M-9", "sku")).contains("M-9");
        });
    }
}
