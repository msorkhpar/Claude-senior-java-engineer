package practice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

record Owner(String name) {
}

record Item(String id, Owner owner) {
}

public final class OwnerNames {

    private OwnerNames() {
    }

    public static List<String> ownerNames(List<Item> items) {
        return items.stream()
                .filter(Objects::nonNull)
                .map(item -> Optional.ofNullable(item.owner())
                        .map(Owner::name)
                        .orElse("Unknown"))
                .toList();
    }
}
