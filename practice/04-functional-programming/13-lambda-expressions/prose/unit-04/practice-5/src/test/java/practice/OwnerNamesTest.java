package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerNamesTest {

    @Test
    void namesEachOwner() {
        List<Item> items = List.of(new Item("a", new Owner("Ann")), new Item("b", new Owner("Ben")));

        assertThat(OwnerNames.ownerNames(items)).containsExactly("Ann", "Ben");
        assertThat(OwnerNames.ownerNames(List.of())).isEmpty();
        assertThat(OwnerNames.ownerNames(List.of(new Item("a", new Owner("Ann")), new Item("b", new Owner("Ann")),
                new Item("c", new Owner("  "))))).containsExactly("Ann", "Ann", "  ");
    }

    @Test
    void nullItemsAreDropped() {
        List<Item> items = Arrays.asList(new Item("a", new Owner("Ann")), null, new Item("c", new Owner("Cy")));

        assertThat(OwnerNames.ownerNames(items)).containsExactly("Ann", "Cy");
    }

    @Test
    void aMissingOwnerIsUnknown() {
        assertThat(OwnerNames.ownerNames(List.of(new Item("a", null)))).containsExactly("Unknown");
    }

    @Test
    void anOwnerWithNoNameIsUnknown() {
        assertThat(OwnerNames.ownerNames(List.of(new Item("a", new Owner(null))))).containsExactly("Unknown");
    }
}
