package practice;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class Person {
    private final String name;
    private final int age;

    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

final class Pet {
    private final String name;

    Pet(String name) {
        this.name = name;
    }
}

final class Shop {
    static final class Item {
        private final String label;

        Item(String label) {
            this.label = label;
        }
    }
}

final class Store {
    static final class Item {
        private final String label;

        Item(String label) {
            this.label = label;
        }
    }
}

class PropertyReaderTest {

    private final Map<Class<?>, Integer> scans = new HashMap<>();
    private final Map<Class<?>, Field[]> handedOut = new HashMap<>();

    private PropertyReader newReader() {
        return new PropertyReader(type -> {
            scans.merge(type, 1, Integer::sum);
            Field[] fields = type.getDeclaredFields();
            handedOut.put(type, fields);
            return fields;
        });
    }

    @Test
    void readsPropertiesByName() {
        PropertyReader reader = newReader();
        Person ann = new Person(new String("Ann"), 41);

        assertThat(reader.read(ann, "name")).isEqualTo("Ann");
        assertThat(reader.read(ann, "age")).isEqualTo(41);
        assertThatThrownBy(() -> reader.read(ann, "salary")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aClassIsScannedOnce() {
        PropertyReader reader = newReader();
        Person ann = new Person("Ann", 41);
        Person bob = new Person("Bob", 1985);

        reader.read(ann, "name");
        reader.read(ann, "age");
        assertThat(reader.read(bob, "age")).isEqualTo(1985);

        assertThat(scans).containsEntry(Person.class, 1);
    }

    @Test
    void twoClassesAreCachedApart() {
        PropertyReader reader = newReader();

        assertThat(reader.read(new Person(new String("Ann"), 41), "name")).isEqualTo("Ann");
        assertThat(reader.read(new Pet(new String("Rex")), "name")).isEqualTo("Rex");
        assertThat(reader.read(new Person(new String("Bob"), 7), "name")).isEqualTo("Bob");
        assertThat(reader.read(new Shop.Item(new String("mug")), "label")).isEqualTo("mug");
        assertThat(reader.read(new Store.Item(new String("pen")), "label")).isEqualTo("pen");
    }

    @Test
    void fieldsAreMadeAccessibleAtScanTime() {
        PropertyReader reader = newReader();
        Person ann = new Person("Ann", 41);

        reader.read(ann, "name");

        assertThat(handedOut.get(Person.class)).hasSize(2).allSatisfy(field -> assertThat(field.canAccess(ann)).isTrue());
    }
}
