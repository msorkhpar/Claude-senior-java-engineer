package practice;

import java.util.List;

record Owner(String name) {
}

record Item(String id, Owner owner) {
}

public final class OwnerNames {

    private OwnerNames() {
    }

    public static List<String> ownerNames(List<Item> items) {
        throw new UnsupportedOperationException("write ownerNames");
    }
}
