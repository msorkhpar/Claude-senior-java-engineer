package practice;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryTest {

    @Test
    void tracksStockThroughItsOwnMethods() {
        Inventory inventory = new Inventory();
        inventory.add("apple", 3);
        inventory.add("pear", 2);
        inventory.add("apple", 2);
        assertThat(inventory.count("apple")).isEqualTo(5);
        assertThat(inventory.remove("apple", 4)).isTrue();
        assertThat(inventory.count("apple")).isEqualTo(1);
        assertThat(inventory.items()).isEqualTo(Map.of("apple", 1, "pear", 2));
    }

    @Test
    void theItemsViewCannotChangeTheInventory() {
        Inventory inventory = new Inventory();
        inventory.add("apple", 1);
        Map<String, Integer> view = inventory.items();
        try {
            view.put("apple", 100);
            view.put("kiwi", 7);
        } catch (UnsupportedOperationException refused) {
            // a read-only view is one good answer
        }
        assertThat(inventory.count("apple")).isEqualTo(1);
        assertThat(inventory.count("kiwi")).isZero();
    }

    @Test
    void anUnknownItemCountsZero() {
        assertThat(new Inventory().count("kiwi")).isZero();
    }

    @Test
    void removingMoreThanThereIsChangesNothing() {
        Inventory inventory = new Inventory();
        inventory.add("pear", 2);
        assertThat(inventory.remove("pear", 10)).isFalse();
        assertThat(inventory.count("pear")).isEqualTo(2);
    }

    @Test
    void anItemRemovedToZeroLeavesTheList() {
        Inventory inventory = new Inventory();
        inventory.add("apple", 1);
        inventory.add("pear", 2);
        assertThat(inventory.remove("pear", 2)).isTrue();
        assertThat(inventory.items()).isEqualTo(Map.of("apple", 1));
    }
}
