package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagerTest {

    private static final List<Integer> ITEMS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    @Test
    void cutsFullPages() {
        assertThat(Pager.page(ITEMS, 0, 3)).containsExactly(1, 2, 3);
        assertThat(Pager.page(ITEMS, 1, 3)).containsExactly(4, 5, 6);
        assertThat(Pager.page(ITEMS, 1, 5)).containsExactly(6, 7, 8, 9, 10);
    }

    @Test
    void theLastPageMayBeShort() {
        assertThat(Pager.page(ITEMS, 3, 3)).containsExactly(10);
        assertThat(Pager.page(ITEMS, 0, 20)).containsExactlyElementsOf(ITEMS);
    }

    @Test
    void aPageBeyondTheEndIsEmpty() {
        assertThat(Pager.page(ITEMS, 5, 3)).isEmpty();
        assertThat(Pager.page(List.of(), 0, 3)).isEmpty();
    }

    @Test
    void hugePageNumbersDoNotOverflow() {
        assertThat(Pager.page(ITEMS, 300_000, 10_000)).isEmpty();
        assertThat(Pager.page(ITEMS, 65_536, 65_536)).isEmpty();
    }
}
