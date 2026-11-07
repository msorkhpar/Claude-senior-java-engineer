package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class WriteCountingMapTest {

    private static Map<String, Boolean> batch() {
        Map<String, Boolean> b = new LinkedHashMap<>();
        b.put("checkout-v2", true);
        b.put("dark-mode", false);
        b.put("beta-search", true);
        b.put("legacy-export", false);
        return b;
    }

    @Test
    void everyPutIsOneWrite() {
        WriteCountingMap<String, Boolean> flags = new WriteCountingMap<>(new HashMap<>());
        assertThat(flags.put("dark-mode", true)).isNull();
        assertThat(flags.put("beta-search", false)).isNull();
        assertThat(flags.put("dark-mode", false)).isTrue();
        assertThat(flags.writes()).isEqualTo(3);
        assertThat(flags.size()).isEqualTo(2);
        assertThat(flags.get("dark-mode")).isFalse();
    }

    @Test
    void writesLandInTheMapItWraps() {
        Map<String, Boolean> backing = new TreeMap<>();
        WriteCountingMap<String, Boolean> flags = new WriteCountingMap<>(backing);
        flags.put("checkout-v2", true);
        assertThat(backing).containsEntry("checkout-v2", true);
        backing.put("legacy-export", false);
        assertThat(flags.get("legacy-export")).isFalse();
        assertThat(flags.size()).isEqualTo(2);
    }

    @Test
    void aBatchIsCountedOncePerMappingWhateverTheMap() {
        WriteCountingMap<String, Boolean> overHash = new WriteCountingMap<>(new HashMap<>());
        overHash.putAll(batch());
        assertThat(overHash.writes()).as("over a HashMap").isEqualTo(4);
        assertThat(overHash.size()).isEqualTo(4);

        WriteCountingMap<String, Boolean> overTree = new WriteCountingMap<>(new TreeMap<>());
        overTree.put("dark-mode", true);
        overTree.putAll(batch());
        assertThat(overTree.writes()).as("over a TreeMap").isEqualTo(5);
        assertThat(overTree.size()).isEqualTo(4);
    }

    @Test
    void equalityIsTheSameFromBothSides() {
        WriteCountingMap<String, Boolean> flags = new WriteCountingMap<>(new HashMap<>());
        flags.putAll(batch());
        Map<String, Boolean> previous = new HashMap<>(batch());
        assertThat(flags.equals(previous)).as("wrapper.equals(previous)").isTrue();
        assertThat(previous.equals(flags)).as("previous.equals(wrapper)").isTrue();
        Map<String, Boolean> changed = new HashMap<>(batch());
        changed.put("dark-mode", true);
        assertThat(flags.equals(changed)).as("a map with another value").isFalse();
    }

    @Test
    void aSnapshotIsFoundInAHashSet() {
        WriteCountingMap<String, Boolean> flags = new WriteCountingMap<>(new TreeMap<>());
        flags.putAll(batch());
        Set<Map<String, Boolean>> snapshots = new HashSet<>();
        snapshots.add(flags);
        assertThat(flags.hashCode()).isEqualTo(new HashMap<>(batch()).hashCode());
        assertThat(snapshots.contains(new HashMap<>(batch()))).isTrue();
    }
}
