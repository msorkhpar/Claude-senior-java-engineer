package com.github.msorkhpar.claudejavatutor.concurrentcollections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Collection Capacity Tests")
class CollectionCapacityTest {

    @Nested
    @DisplayName("HashMap Capacity")
    class HashMapCapacityTest {

        private final CollectionCapacity.HashMapCapacity capacity = new CollectionCapacity.HashMapCapacity();

        @Test
        @DisplayName("Should create HashMap with default capacity")
        void testCreateWithDefaults() {
            Map<String, Integer> map = capacity.createWithDefaults();

            assertThat(map).isEmpty();
            assertThat(map).isInstanceOf(HashMap.class);
        }

        @Test
        @DisplayName("Should create HashMap with specified capacity")
        void testCreateWithCapacity() {
            Map<String, Integer> map = capacity.createWithCapacity(32);

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("Should create HashMap with capacity and load factor")
        void testCreateWithCapacityAndLoadFactor() {
            Map<String, Integer> map = capacity.createWithCapacityAndLoadFactor(32, 0.5f);

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for negative capacity")
        void testNegativeCapacity() {
            assertThatThrownBy(() -> capacity.createWithCapacity(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid load factor")
        void testInvalidLoadFactor() {
            assertThatThrownBy(() -> capacity.createWithCapacityAndLoadFactor(16, 0.0f))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should add elements efficiently with pre-sized capacity")
        void testAddElementsEfficiently() {
            int count = capacity.addElementsEfficiently(100);

            assertThat(count).isEqualTo(100);
        }

        @Test
        @DisplayName("Next power of 2 should round up correctly")
        void testNextPowerOfTwo() {
            assertThat(capacity.nextPowerOfTwo(10)).isEqualTo(16);
            assertThat(capacity.nextPowerOfTwo(7)).isEqualTo(8);
            assertThat(capacity.nextPowerOfTwo(17)).isEqualTo(32);
        }

        @Test
        @DisplayName("Next power of 2 for exact power should return same value")
        void testNextPowerOfTwoExact() {
            assertThat(capacity.nextPowerOfTwo(16)).isEqualTo(16);
            assertThat(capacity.nextPowerOfTwo(8)).isEqualTo(8);
            assertThat(capacity.nextPowerOfTwo(1)).isEqualTo(1);
        }

        @Test
        @DisplayName("Next power of 2 for zero or negative should return 1")
        void testNextPowerOfTwoEdge() {
            assertThat(capacity.nextPowerOfTwo(0)).isEqualTo(1);
            assertThat(capacity.nextPowerOfTwo(-5)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("HashMap Resizing")
    class HashMapResizingTest {

        private final CollectionCapacity.HashMapResizing resizing = new CollectionCapacity.HashMapResizing();

        @Test
        @DisplayName("Should calculate expected capacity thresholds")
        void testExpectedCapacityThresholds() {
            // With initial capacity 4, load factor 0.75:
            // threshold at 3 elements -> resize to 8
            // threshold at 6 elements -> resize to 16
            // threshold at 12 elements -> resize to 32
            List<Integer> thresholds = resizing.expectedCapacityThresholds(4, 0.75f, 20);

            assertThat(thresholds).isNotEmpty();
            assertThat(thresholds.get(0)).isEqualTo(4);
            // Each subsequent threshold should be double the previous
            for (int i = 1; i < thresholds.size(); i++) {
                assertThat(thresholds.get(i)).isEqualTo(thresholds.get(i - 1) * 2);
            }
            // Final capacity should be able to hold 20 elements
            int finalCapacity = thresholds.get(thresholds.size() - 1);
            assertThat((int) (finalCapacity * 0.75f)).isGreaterThanOrEqualTo(20);
        }

        @Test
        @DisplayName("Should create map with high load factor")
        void testCreateHighLoadFactor() {
            Map<String, Integer> map = resizing.createHighLoadFactor();

            // Just verify it works and accepts entries
            for (int i = 0; i < 20; i++) {
                map.put("key" + i, i);
            }
            assertThat(map).hasSize(20);
        }

        @Test
        @DisplayName("Should create map with low load factor")
        void testCreateLowLoadFactor() {
            Map<String, Integer> map = resizing.createLowLoadFactor();

            for (int i = 0; i < 20; i++) {
                map.put("key" + i, i);
            }
            assertThat(map).hasSize(20);
        }

        @Test
        @DisplayName("Should calculate optimal initial capacity")
        void testOptimalInitialCapacity() {
            int optimal = resizing.optimalInitialCapacity(100, 0.75f);

            // 100 / 0.75 + 1 = 134
            assertThat(optimal).isEqualTo(134);
        }

        @Test
        @DisplayName("Should handle zero elements - only initial capacity")
        void testExpectedCapacityThresholdsZero() {
            List<Integer> thresholds = resizing.expectedCapacityThresholds(4, 0.75f, 0);

            // 0 elements fit within initial capacity (threshold = 3)
            assertThat(thresholds).containsExactly(4);
        }
    }

    @Nested
    @DisplayName("Other Collection Capacity")
    class OtherCollectionCapacityTest {

        private final CollectionCapacity.OtherCollectionCapacity other =
                new CollectionCapacity.OtherCollectionCapacity();

        @Test
        @DisplayName("Should create ArrayList with specified capacity")
        void testCreateArrayListWithCapacity() {
            List<String> list = other.createArrayListWithCapacity(100);

            assertThat(list).isEmpty();
            // Capacity is internal, we just verify it works
        }

        @Test
        @DisplayName("ArrayList should grow automatically")
        void testArrayListGrowth() {
            int count = other.arrayListGrowth(1000);

            assertThat(count).isEqualTo(1000);
        }

        @Test
        @DisplayName("Should trim ArrayList capacity to match size")
        void testTrimArrayList() {
            ArrayList<String> list = other.trimArrayList("a", "b", "c");

            assertThat(list).containsExactly("a", "b", "c");
            assertThat(list).hasSize(3);
        }

        @Test
        @DisplayName("Should create ConcurrentHashMap with custom parameters")
        void testCreateConcurrentMapWithParams() {
            ConcurrentHashMap<String, Integer> map = other.createConcurrentMapWithParams(32, 0.75f, 8);

            map.put("key", 1);
            assertThat(map.get("key")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should create HashSet with initial capacity")
        void testCreateHashSetWithCapacity() {
            Set<String> set = other.createHashSetWithCapacity(64);

            assertThat(set).isEmpty();
            set.add("test");
            assertThat(set).hasSize(1);
        }

        @Test
        @DisplayName("Should create pre-sized map for known data")
        void testPreSizedVsDefault() {
            List<String> keys = Arrays.asList("a", "b", "c", "d", "e");
            Map<String, Integer> map = other.preSizedVsDefault(keys);

            assertThat(map).hasSize(5);
            assertThat(map.get("a")).isEqualTo(0);
            assertThat(map.get("e")).isEqualTo(4);
        }

        @Test
        @DisplayName("Should handle empty key list in pre-sized map")
        void testPreSizedEmpty() {
            Map<String, Integer> map = other.preSizedVsDefault(Collections.emptyList());

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("LinkedList has no capacity concept")
        void testLinkedListNoCapacity() {
            LinkedList<String> list = other.linkedListNoCapacity("a", "b", "c");

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("CopyOnWriteArrayList internal array matches size exactly")
        void testCowAlwaysExactSize() {
            CopyOnWriteArrayList<String> list = other.cowAlwaysExactSize("a", "b");

            assertThat(list).hasSize(2);
            list.add("c");
            assertThat(list).hasSize(3);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for negative ArrayList capacity")
        void testNegativeArrayListCapacity() {
            assertThatThrownBy(() -> other.createArrayListWithCapacity(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
