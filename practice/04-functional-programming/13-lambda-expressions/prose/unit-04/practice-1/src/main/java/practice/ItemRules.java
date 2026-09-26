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
        throw new UnsupportedOperationException("write isValidItem");
    }

    public static List<String> validIds(List<Item> items) {
        throw new UnsupportedOperationException("write validIds");
    }
}
