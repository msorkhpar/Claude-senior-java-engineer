package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs in name order, so the test that tries to change the cache runs last. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class HttpStatusTest {

    @Test
    void looksUpAndGroupsStatuses() {
        assertThat(HttpStatus.fromCode(404)).contains(HttpStatus.NOT_FOUND);
        assertThat(HttpStatus.fromCode(Integer.parseInt("503"))).contains(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(HttpStatus.byCategory(HttpStatus.Category.SUCCESS))
                .containsExactly(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.NO_CONTENT);
        assertThat(HttpStatus.byCategory(HttpStatus.Category.SERVER_ERROR))
                .containsExactly(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(HttpStatus.all()).containsExactly(HttpStatus.values());
    }

    @Test
    void anUnknownCodeIsEmpty() {
        assertThat(HttpStatus.fromCode(418)).isEmpty();
    }

    @Test
    void whatAllGivesCannotCorruptTheCache() {
        List<HttpStatus> first = HttpStatus.all();
        try {
            first.set(0, HttpStatus.NOT_FOUND);
        } catch (UnsupportedOperationException refused) {
            // a read-only list is one way to keep the cache safe
        }
        assertThat(HttpStatus.all()).containsExactly(HttpStatus.values());
    }
}
