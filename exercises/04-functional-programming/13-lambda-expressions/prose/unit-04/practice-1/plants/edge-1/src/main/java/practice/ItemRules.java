package practice;

import java.util.List;

enum Status { ACTIVE, DELETED }

record Owner(String name, boolean verified) {
}

record Item(String id, int value, boolean active, Status status, Owner owner) {
}

public final class ItemRules {

    private ItemRules() {
    }

    public static boolean isValidItem(Item item) {
        return item.value() > 0
                && item.active()
                && item.status() != Status.DELETED
                && item.owner() != null
                && item.owner().verified();
    }

    public static List<String> validIds(List<Item> items) {
        return items.stream()
                .filter(ItemRules::isValidItem)
                .map(Item::id)
                .toList();
    }
}
