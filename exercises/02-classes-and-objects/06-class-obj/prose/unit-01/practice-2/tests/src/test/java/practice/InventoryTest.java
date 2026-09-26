package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

    @Test
    void addsAndRemovesStock() {
        Inventory inventory = new Inventory();
        inventory.add("bolt", 5);
        inventory.add("nut", 2);
        inventory.add("bolt", 1);
        inventory.remove("bolt", 2);
        assertThat(inventory.quantityOf("bolt")).isEqualTo(4);
        assertThat(inventory.quantityOf("nut")).isEqualTo(2);
        assertThat(inventory.items()).containsExactly("bolt", "nut");
    }

    @Test
    void aNewInventoryIsEmptyAndUsable() {
        Inventory inventory = new Inventory();
        assertThat(inventory.quantityOf("bolt")).isZero();
        assertThat(inventory.items()).isEmpty();
    }

    @Test
    void removingMoreThanHeldIsRefusedAndChangesNothing() {
        Inventory inventory = new Inventory();
        inventory.add("bolt", 3);
        assertThatThrownBy(() -> inventory.remove("bolt", 4)).isInstanceOf(IllegalArgumentException.class);
        assertThat(inventory.quantityOf("bolt")).isEqualTo(3);
        assertThatThrownBy(() -> inventory.remove("washer", 1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(inventory.quantityOf("washer")).isZero();
    }

    @Test
    void itemsAreMatchedByNameNotByObject() {
        Inventory inventory = new Inventory();
        String added = new String("bolt");
        String asked = new StringBuilder("bo").append("lt").toString();
        inventory.add(added, 3);
        inventory.add(new String("bolt"), 2);
        assertThat(inventory.quantityOf(asked)).isEqualTo(5);
        inventory.remove(new String("bolt"), 5);
        assertThat(inventory.items()).isEmpty();
    }

    @Test
    void anItemRemovedToZeroIsNoLongerListed() {
        Inventory inventory = new Inventory();
        inventory.add("bolt", 3);
        inventory.add("nut", 1);
        inventory.remove("bolt", 3);
        assertThat(inventory.quantityOf("bolt")).isZero();
        assertThat(inventory.items()).containsExactly("nut");
    }
}
