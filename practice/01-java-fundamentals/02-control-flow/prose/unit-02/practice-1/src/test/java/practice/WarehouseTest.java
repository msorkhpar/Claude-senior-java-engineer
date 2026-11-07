package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseTest {

    @Test
    void reportsHowManyShelvesItRaisedAndAFullCount() {
        Warehouse warehouse = new Warehouse(new int[]{3, 0, 12, 7, 4}, List.of(4, 9, 2));

        assertThat(warehouse.morningReport(5)).isEqualTo("3 shelves restocked, 15 items counted");
    }

    @Test
    void theRaisedLevelsAreThereForTheNextReader() {
        int[] levels = {3, 0, 12, 7, 4};

        new Warehouse(levels, List.of()).raiseToMinimum(levels, 5);

        assertThat(levels).containsExactly(5, 5, 12, 7, 5);
    }

    @Test
    void shelvesNobodyCountedAreLeftOutOfTheTotal() {
        List<Integer> counts = Arrays.asList(6, null, 1, null, 11);

        assertThat(new Warehouse(new int[0], counts).countedTotal(counts)).isEqualTo(18);
    }
}
