package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationTest {

    private static final List<Integer> ORDERS = IntStream.rangeClosed(1, 300).boxed().toList();

    private static Map<String, Object> query(Object... keysAndValues) {
        Map<String, Object> query = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            query.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return query;
    }

    @Test
    void usesTheRequestedPageSize() {
        Pagination pagination = new Pagination(40);

        assertThat(pagination.pageSizeFor(query("pageSize", 120))).isEqualTo(120);
        assertThat(pagination.firstPage(ORDERS, query("pageSize", 7, "sort", "date"))).hasSize(7);
    }

    @Test
    void usesTheDefaultWhenTheRequestAsksForNone() {
        Pagination pagination = new Pagination(40);

        assertThat(pagination.pageSizeFor(query("sort", "date"))).isEqualTo(40);
        assertThat(pagination.firstPage(ORDERS, query())).hasSize(40);
    }

    @Test
    void usesTheDefaultForAValueItCannotUse() {
        Pagination pagination = new Pagination(40);

        assertThat(pagination.pageSizeFor(query("pageSize", 0))).isEqualTo(40);
        assertThat(pagination.pageSizeFor(query("pageSize", 501))).isEqualTo(40);
        assertThat(pagination.pageSizeFor(query("pageSize", "75"))).isEqualTo(40);
        assertThat(pagination.pageSizeFor(query("pageSize", 120L))).isEqualTo(40);
    }

    @Test
    void aWarningShowsWhatTheClientSent() {
        Pagination pagination = new Pagination(40);

        pagination.pageSizeFor(query("pageSize", 900));
        pagination.pageSizeFor(query("pageSize", "75"));

        assertThat(pagination.warnings()).hasSize(2);
        assertThat(pagination.warnings().get(0)).contains("900");
        assertThat(pagination.warnings().get(1)).contains("75");
    }

    @Test
    void aRequestWithoutAPageSizeLeavesNoWarning() {
        Pagination pagination = new Pagination(40);

        pagination.pageSizeFor(query("sort", "date"));
        pagination.pageSizeFor(query());

        assertThat(pagination.warnings()).isEmpty();
    }
}
