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
        Item ownerless = new Item("a", 10, true, Status.ACTIVE, null);
        assertThat(ItemRules.isValidItem(ownerless)).isFalse();
        assertThat(ItemRules.validIds(List.of(ownerless, new Item("b", 10, true, Status.ACTIVE, ANN))))
                .containsExactly("b");
    }

    @Test
    void deletedItemsAreNotValid() {
        Item deleted = new Item("a", 10, true, Status.DELETED, ANN);
        Item noStatus = new Item("n", 10, true, null, ANN);

        assertThat(ItemRules.isValidItem(deleted)).isFalse();
        assertThat(ItemRules.isValidItem(noStatus)).isTrue();
        assertThat(ItemRules.validIds(List.of(deleted, noStatus, new Item("b", 10, true, Status.ACTIVE, ANN))))
                .containsExactly("n", "b");
    }

    @Test
    void zeroValueIsNotValid() {
        Item zero = new Item("a", 0, true, Status.ACTIVE, ANN);

        assertThat(ItemRules.isValidItem(zero)).isFalse();
        assertThat(ItemRules.validIds(List.of(zero, new Item("b", 10, true, Status.ACTIVE, ANN))))
                .containsExactly("b");
    }
}
