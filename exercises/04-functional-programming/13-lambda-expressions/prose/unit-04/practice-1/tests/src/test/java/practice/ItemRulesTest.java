package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRulesTest {

    private static final Owner ANN = new Owner("Ann", true);
    private static final Owner BEN = new Owner("Ben", false);

    @Test
    void keepsOnlyValidItems() {
        List<Item> items = List.of(
                new Item("a", 10, true, Status.ACTIVE, ANN),
                new Item("b", 10, false, Status.ACTIVE, ANN),
                new Item("c", -5, true, Status.ACTIVE, ANN),
                new Item("d", 10, true, Status.ACTIVE, BEN),
                new Item("e", 1, true, Status.ACTIVE, ANN));

        assertThat(ItemRules.validIds(items)).containsExactly("a", "e");
        assertThat(ItemRules.isValidItem(items.get(0))).isTrue();
        assertThat(ItemRules.isValidItem(items.get(3))).isFalse();
    }

    @Test
    void nullItemsAreSkipped() {
        List<Item> items = Arrays.asList(null, new Item("a", 10, true, Status.ACTIVE, ANN));

        assertThat(ItemRules.isValidItem(null)).isFalse();
        assertThat(ItemRules.validIds(items)).containsExactly("a");
    }

    @Test
    void anItemWithNoOwnerIsNotValid() {
        assertThat(ItemRules.isValidItem(new Item("a", 10, true, Status.ACTIVE, null))).isFalse();
    }

    @Test
    void deletedItemsAreNotValid() {
        assertThat(ItemRules.isValidItem(new Item("a", 10, true, Status.DELETED, ANN))).isFalse();
    }

    @Test
    void zeroValueIsNotValid() {
        assertThat(ItemRules.isValidItem(new Item("a", 0, true, Status.ACTIVE, ANN))).isFalse();
    }
}
