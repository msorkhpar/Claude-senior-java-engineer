package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryParamTest {

    private static Map<String, String> query() {
        return Map.of(
                "page", "7",
                "limit", "250",
                "since", "2025-11-30",
                "customer", "ACME-204",
                "express", "true");
    }

    /**
     * Calls get from code that is itself generic, as a framework's binding layer does. Nothing
     * here checks the result's type, so only get itself can refuse a wrong request.
     */
    private static <T> Object bind(QueryParam param, Map<String, String> query, Class<T> type) {
        return param.get(query, type);
    }

    @Test
    void eachParameterComesBackAsTheTypeItHolds() {
        Integer page = QueryParam.PAGE.get(query(), Integer.class);
        LocalDate since = QueryParam.SINCE.get(query(), LocalDate.class);
        String customer = QueryParam.CUSTOMER.get(query(), String.class);
        Boolean express = QueryParam.EXPRESS.get(query(), Boolean.class);

        assertThat(page).isEqualTo(7);
        assertThat(since).isEqualTo(LocalDate.of(2025, 11, 30));
        assertThat(customer).isEqualTo("ACME-204");
        assertThat(express).isTrue();
        assertThat(QueryParam.LIMIT.get(Map.of("page", "2"), Integer.class)).isNull();
    }

    @Test
    void askingForATypeTheParameterDoesNotHoldFailsInsideGet() {
        assertThatThrownBy(() -> bind(QueryParam.LIMIT, query(), String.class))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> bind(QueryParam.CUSTOMER, query(), LocalDate.class))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> bind(QueryParam.SINCE, query(), boolean.class))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void callersMayAskForAPrimitiveType() {
        int page = QueryParam.PAGE.get(query(), int.class);
        boolean express = QueryParam.EXPRESS.get(query(), boolean.class);

        assertThat(page).isEqualTo(7);
        assertThat(express).isTrue();
        assertThat(bind(QueryParam.LIMIT, query(), int.class)).isEqualTo(250);
    }

    @Test
    void callersMayAskForAWiderTypeThanTheValuesOwn() {
        Number limit = QueryParam.LIMIT.get(query(), Number.class);
        Comparable<?> since = QueryParam.SINCE.get(query(), Comparable.class);
        Object customer = QueryParam.CUSTOMER.get(query(), Object.class);

        assertThat(limit).isEqualTo(250);
        assertThat(since).isEqualTo(LocalDate.of(2025, 11, 30));
        assertThat(customer).isEqualTo("ACME-204");
    }
}
