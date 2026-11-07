package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseDirectoryTest {

    /** A set that hands out its codes in exactly the order given, like one instance's view of the service. */
    private static Set<String> arriving(String... codes) {
        return new LinkedHashSet<>(List.of(codes));
    }

    @Test
    void codesThatArriveInOrderComeBackAsTheFirstPage() {
        WarehouseDirectory dir = new WarehouseDirectory(arriving("BER-1", "LIS-2", "OSL-1", "ROM-4"));

        assertThat(dir.page(0, 3)).containsExactly("BER-1", "LIS-2", "OSL-1");
        assertThat(dir.page(7, 3)).isEmpty();
    }

    @Test
    void theFirstPageIsAlphabeticalWhateverOrderTheCodesArriveIn() {
        WarehouseDirectory dir = new WarehouseDirectory(
                arriving("VIE-2", "AMS-1", "OSL-1", "DUB-3", "MAD-1", "BER-1", "ZRH-1"));

        assertThat(dir.page(0, 3)).containsExactly("AMS-1", "BER-1", "DUB-3");
    }

    @Test
    void aLaterPageHoldsTheNextCodesAlphabetically() {
        WarehouseDirectory dir = new WarehouseDirectory(
                arriving("VIE-2", "AMS-1", "OSL-1", "DUB-3", "MAD-1", "BER-1", "ZRH-1"));

        assertThat(dir.page(1, 3)).containsExactly("MAD-1", "OSL-1", "VIE-2");
    }

    @Test
    void pagingThroughShowsEveryCodeOnce() {
        WarehouseDirectory dir = new WarehouseDirectory(
                arriving("LIS-2", "BER-1", "ROM-4", "AMS-1", "PRG-1"));

        List<String> seen = new ArrayList<>();
        for (int p = 0; p < dir.pageCount(2); p++) seen.addAll(dir.page(p, 2));

        assertThat(seen).containsExactly("AMS-1", "BER-1", "LIS-2", "PRG-1", "ROM-4");
    }
}
