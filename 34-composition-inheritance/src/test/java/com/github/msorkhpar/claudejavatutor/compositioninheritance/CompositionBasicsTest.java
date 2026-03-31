package com.github.msorkhpar.claudejavatutor.compositioninheritance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Composition Basics Tests (8.4.1)")
class CompositionBasicsTest {

    // ---- Inheritance fragile base class problem ----

    @Nested
    @DisplayName("Inheritance-based CountingStack (fragile)")
    class CountingInheritanceStackTest {

        @Test
        @DisplayName("Should demonstrate double-counting bug with pushAll via inheritance")
        void testPushAllDoubleCountingBug() {
            var stack = new CompositionBasics.CountingInheritanceStack<String>();
            stack.pushAll(List.of("a", "b", "c"));

            // The bug: pushAll increments by 3, then calls push() 3 times, each incrementing again
            // So pushCount is 6 instead of the expected 3
            assertThat(stack.getPushCount()).isEqualTo(6);
            assertThat(stack.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should count correctly for single push operations")
        void testSinglePushCounting() {
            var stack = new CompositionBasics.CountingInheritanceStack<Integer>();
            stack.push(1);
            stack.push(2);

            assertThat(stack.getPushCount()).isEqualTo(2);
            assertThat(stack.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle pop on empty stack")
        void testPopEmptyStack() {
            var stack = new CompositionBasics.CountingInheritanceStack<String>();

            assertThatThrownBy(stack::pop)
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Stack is empty");
        }
    }

    // ---- Composition-based CountingStack (correct) ----

    @Nested
    @DisplayName("Composition-based CountingStack (correct)")
    class CountingStackTest {

        @Test
        @DisplayName("Should count pushAll correctly without double-counting")
        void testPushAllCountingCorrect() {
            var counting = new CompositionBasics.CountingStack<>(new CompositionBasics.SimpleStack<>());
            counting.pushAll(List.of("a", "b", "c"));

            assertThat(counting.getPushCount()).isEqualTo(3);
            assertThat(counting.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should count individual pushes correctly")
        void testIndividualPushCounting() {
            var counting = new CompositionBasics.CountingStack<>(new CompositionBasics.SimpleStack<>());
            counting.push("x");
            counting.push("y");

            assertThat(counting.getPushCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count mixed push and pushAll correctly")
        void testMixedPushAndPushAll() {
            var counting = new CompositionBasics.CountingStack<>(new CompositionBasics.SimpleStack<>());
            counting.push("a");
            counting.pushAll(List.of("b", "c"));
            counting.push("d");

            assertThat(counting.getPushCount()).isEqualTo(4);
            assertThat(counting.size()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should pop elements in LIFO order")
        void testPopOrder() {
            var counting = new CompositionBasics.CountingStack<>(new CompositionBasics.SimpleStack<>());
            counting.push("first");
            counting.push("second");
            counting.push("third");

            assertThat(counting.pop()).isEqualTo("third");
            assertThat(counting.pop()).isEqualTo("second");
            assertThat(counting.pop()).isEqualTo("first");
        }

        @Test
        @DisplayName("Should throw on pop from empty stack")
        void testPopEmptyStack() {
            var counting = new CompositionBasics.CountingStack<>(new CompositionBasics.SimpleStack<>());

            assertThatThrownBy(counting::pop)
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Stack is empty");
        }

        @Test
        @DisplayName("Should report isEmpty correctly")
        void testIsEmpty() {
            var counting = new CompositionBasics.CountingStack<>(new CompositionBasics.SimpleStack<>());
            assertThat(counting.isEmpty()).isTrue();

            counting.push("item");
            assertThat(counting.isEmpty()).isFalse();

            counting.pop();
            assertThat(counting.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should reject null delegate")
        void testNullDelegate() {
            assertThatThrownBy(() -> new CompositionBasics.CountingStack<>(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Delegate stack must not be null");
        }

        @Test
        @DisplayName("Should handle pushAll with empty collection")
        void testPushAllEmptyCollection() {
            var counting = new CompositionBasics.CountingStack<>(new CompositionBasics.SimpleStack<>());
            counting.pushAll(Collections.emptyList());

            assertThat(counting.getPushCount()).isEqualTo(0);
            assertThat(counting.size()).isEqualTo(0);
        }
    }

    // ---- SimpleStack ----

    @Nested
    @DisplayName("SimpleStack")
    class SimpleStackTest {

        @Test
        @DisplayName("Should push and pop elements correctly")
        void testPushAndPop() {
            var stack = new CompositionBasics.SimpleStack<Integer>();
            stack.push(1);
            stack.push(2);
            stack.push(3);

            assertThat(stack.pop()).isEqualTo(3);
            assertThat(stack.pop()).isEqualTo(2);
            assertThat(stack.pop()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle pushAll")
        void testPushAll() {
            var stack = new CompositionBasics.SimpleStack<String>();
            stack.pushAll(List.of("a", "b", "c"));

            assertThat(stack.size()).isEqualTo(3);
            assertThat(stack.pop()).isEqualTo("c");
        }

        @Test
        @DisplayName("Should track size correctly")
        void testSize() {
            var stack = new CompositionBasics.SimpleStack<String>();
            assertThat(stack.size()).isEqualTo(0);

            stack.push("a");
            assertThat(stack.size()).isEqualTo(1);

            stack.pop();
            assertThat(stack.size()).isEqualTo(0);
        }
    }

    // ---- TextProcessor (strategy composition) ----

    @Nested
    @DisplayName("TextProcessor")
    class TextProcessorTest {

        @Test
        @DisplayName("Should apply single transformation")
        void testSingleTransformation() {
            var processor = new CompositionBasics.TextProcessor()
                    .addTransformation(String::toUpperCase);

            assertThat(processor.process("hello")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should chain multiple transformations in order")
        void testChainedTransformations() {
            var processor = new CompositionBasics.TextProcessor()
                    .addTransformation(String::trim)
                    .addTransformation(String::toUpperCase)
                    .addTransformation(s -> s.replace(" ", "_"));

            assertThat(processor.process("  hello world  ")).isEqualTo("HELLO_WORLD");
        }

        @Test
        @DisplayName("Should return null for null input")
        void testNullInput() {
            var processor = new CompositionBasics.TextProcessor()
                    .addTransformation(String::toUpperCase);

            assertThat(processor.process(null)).isNull();
        }

        @Test
        @DisplayName("Should process without transformations returning original")
        void testNoTransformations() {
            var processor = new CompositionBasics.TextProcessor();

            assertThat(processor.process("unchanged")).isEqualTo("unchanged");
        }

        @Test
        @DisplayName("Should reject null transformation")
        void testNullTransformation() {
            var processor = new CompositionBasics.TextProcessor();

            assertThatThrownBy(() -> processor.addTransformation(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Transformation must not be null");
        }

        @Test
        @DisplayName("Should return unmodifiable transformation list")
        void testUnmodifiableTransformationList() {
            var processor = new CompositionBasics.TextProcessor()
                    .addTransformation(String::toUpperCase);

            assertThatThrownBy(() -> processor.getTransformations().add(String::toLowerCase))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should handle empty string")
        void testEmptyString() {
            var processor = new CompositionBasics.TextProcessor()
                    .addTransformation(String::toUpperCase);

            assertThat(processor.process("")).isEqualTo("");
        }
    }

    // ---- InstrumentedSet (forwarding wrapper) ----

    @Nested
    @DisplayName("InstrumentedSet (forwarding wrapper)")
    class InstrumentedSetTest {

        @Test
        @DisplayName("Should count single add operations")
        void testSingleAdd() {
            var set = new CompositionBasics.InstrumentedSet<>(new HashSet<>());
            set.add("a");
            set.add("b");

            assertThat(set.getAddCount()).isEqualTo(2);
            assertThat(set).hasSize(2);
        }

        @Test
        @DisplayName("Should count addAll correctly without double-counting")
        void testAddAllNoDuplication() {
            var set = new CompositionBasics.InstrumentedSet<>(new HashSet<>());
            set.addAll(List.of("a", "b", "c"));

            // Correct count: 3 (not 6 as would happen with inheritance-based HashSet)
            assertThat(set.getAddCount()).isEqualTo(3);
            assertThat(set).hasSize(3);
        }

        @Test
        @DisplayName("Should count mixed add and addAll")
        void testMixedAddAndAddAll() {
            var set = new CompositionBasics.InstrumentedSet<>(new HashSet<>());
            set.add("x");
            set.addAll(List.of("y", "z"));

            assertThat(set.getAddCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should delegate contains correctly")
        void testContains() {
            var set = new CompositionBasics.InstrumentedSet<>(new HashSet<>());
            set.add("hello");

            assertThat(set.contains("hello")).isTrue();
            assertThat(set.contains("world")).isFalse();
        }

        @Test
        @DisplayName("Should delegate remove correctly")
        void testRemove() {
            var set = new CompositionBasics.InstrumentedSet<>(new HashSet<>());
            set.add("a");
            set.remove("a");

            assertThat(set).isEmpty();
            // addCount stays at 1 (remove is not counted)
            assertThat(set.getAddCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should delegate clear correctly")
        void testClear() {
            var set = new CompositionBasics.InstrumentedSet<>(new HashSet<>());
            set.addAll(List.of("a", "b", "c"));
            set.clear();

            assertThat(set).isEmpty();
            assertThat(set.getAddCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should delegate iterator correctly")
        void testIterator() {
            var set = new CompositionBasics.InstrumentedSet<>(new TreeSet<String>());
            set.addAll(List.of("c", "a", "b"));

            List<String> elements = new ArrayList<>();
            set.iterator().forEachRemaining(elements::add);

            assertThat(elements).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should work with different underlying Set implementations")
        void testWithTreeSet() {
            var set = new CompositionBasics.InstrumentedSet<>(new TreeSet<>());
            set.addAll(List.of("banana", "apple", "cherry"));

            assertThat(set.getAddCount()).isEqualTo(3);
            // TreeSet maintains sorted order
            assertThat(set.iterator().next()).isEqualTo("apple");
        }

        @Test
        @DisplayName("Should reject null delegate in ForwardingSet")
        void testNullDelegate() {
            assertThatThrownBy(() -> new CompositionBasics.ForwardingSet<>(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Delegate set must not be null");
        }

        @Test
        @DisplayName("Should count duplicate add attempts")
        void testDuplicateAdd() {
            var set = new CompositionBasics.InstrumentedSet<>(new HashSet<>());
            set.add("a");
            set.add("a"); // duplicate, won't increase size but will increase count

            assertThat(set.getAddCount()).isEqualTo(2);
            assertThat(set).hasSize(1);
        }
    }
}
