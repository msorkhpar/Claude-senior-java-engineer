package com.github.msorkhpar.claudejavatutor.compositioninheritance;

import java.util.*;
import java.util.function.Function;

/**
 * Demonstrates the definition and purpose of Composition over Inheritance (8.4.1).
 * Shows how object composition provides flexibility, loose coupling, and avoids
 * the fragile base class problem inherent in deep inheritance hierarchies.
 */
public class CompositionBasics {

    // ---- Inheritance-based approach (problematic) ----

    /**
     * Base class in an inheritance hierarchy. Illustrates the fragile base class problem:
     * subclasses depend on implementation details of the parent.
     */
    public static class InheritanceStack<E> {
        private final List<E> elements = new ArrayList<>();

        public void push(E item) {
            elements.add(item);
        }

        public void pushAll(Collection<E> items) {
            for (E item : items) {
                push(item); // calls push for each item
            }
        }

        public E pop() {
            if (elements.isEmpty()) {
                throw new NoSuchElementException("Stack is empty");
            }
            return elements.remove(elements.size() - 1);
        }

        public int size() {
            return elements.size();
        }

        public boolean isEmpty() {
            return elements.isEmpty();
        }
    }

    /**
     * Subclass that tries to count pushes via inheritance. This is fragile because
     * pushAll() internally calls push(), leading to double-counting.
     */
    public static class CountingInheritanceStack<E> extends InheritanceStack<E> {
        private int pushCount = 0;

        @Override
        public void push(E item) {
            pushCount++;
            super.push(item);
        }

        @Override
        public void pushAll(Collection<E> items) {
            pushCount += items.size();
            super.pushAll(items); // BUG: super.pushAll calls push(), incrementing again
        }

        public int getPushCount() {
            return pushCount;
        }
    }

    // ---- Composition-based approach (preferred) ----

    /**
     * Interface defining a stack contract. Composition targets interfaces, not concrete classes.
     */
    public interface Stack<E> {
        void push(E item);
        void pushAll(Collection<E> items);
        E pop();
        int size();
        boolean isEmpty();
    }

    /**
     * A simple stack implementation using composition (wrapping an ArrayList).
     */
    public static class SimpleStack<E> implements Stack<E> {
        private final List<E> elements = new ArrayList<>();

        @Override
        public void push(E item) {
            elements.add(item);
        }

        @Override
        public void pushAll(Collection<E> items) {
            elements.addAll(items);
        }

        @Override
        public E pop() {
            if (elements.isEmpty()) {
                throw new NoSuchElementException("Stack is empty");
            }
            return elements.remove(elements.size() - 1);
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public boolean isEmpty() {
            return elements.isEmpty();
        }
    }

    /**
     * Counting stack using composition (delegation). Wraps any Stack implementation
     * and reliably counts pushes without depending on internal implementation details.
     */
    public static class CountingStack<E> implements Stack<E> {
        private final Stack<E> delegate;
        private int pushCount = 0;

        public CountingStack(Stack<E> delegate) {
            Objects.requireNonNull(delegate, "Delegate stack must not be null");
            this.delegate = delegate;
        }

        @Override
        public void push(E item) {
            pushCount++;
            delegate.push(item);
        }

        @Override
        public void pushAll(Collection<E> items) {
            pushCount += items.size();
            delegate.pushAll(items);
        }

        @Override
        public E pop() {
            return delegate.pop();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        public int getPushCount() {
            return pushCount;
        }
    }

    // ---- Strategy via composition ----

    /**
     * Demonstrates composing behaviors at runtime using functional interfaces (strategy pattern).
     * Instead of creating subclasses for each transformation, compose transformers.
     */
    public static class TextProcessor {
        private final List<Function<String, String>> transformations = new ArrayList<>();

        public TextProcessor addTransformation(Function<String, String> transformation) {
            Objects.requireNonNull(transformation, "Transformation must not be null");
            transformations.add(transformation);
            return this;
        }

        public String process(String input) {
            if (input == null) {
                return null;
            }
            String result = input;
            for (Function<String, String> t : transformations) {
                result = t.apply(result);
            }
            return result;
        }

        public List<Function<String, String>> getTransformations() {
            return Collections.unmodifiableList(transformations);
        }
    }

    // ---- Forwarding wrapper (Effective Java Item 18 pattern) ----

    /**
     * A forwarding wrapper that delegates all calls to an underlying Set.
     * Subclasses can override specific methods without fragile-base-class issues.
     */
    public static class ForwardingSet<E> implements Set<E> {
        private final Set<E> delegate;

        public ForwardingSet(Set<E> delegate) {
            Objects.requireNonNull(delegate, "Delegate set must not be null");
            this.delegate = delegate;
        }

        @Override public int size() { return delegate.size(); }
        @Override public boolean isEmpty() { return delegate.isEmpty(); }
        @Override public boolean contains(Object o) { return delegate.contains(o); }
        @Override public Iterator<E> iterator() { return delegate.iterator(); }
        @Override public Object[] toArray() { return delegate.toArray(); }
        @Override public <T> T[] toArray(T[] a) { return delegate.toArray(a); }
        @Override public boolean add(E e) { return delegate.add(e); }
        @Override public boolean remove(Object o) { return delegate.remove(o); }
        @Override public boolean containsAll(Collection<?> c) { return delegate.containsAll(c); }
        @Override public boolean addAll(Collection<? extends E> c) { return delegate.addAll(c); }
        @Override public boolean retainAll(Collection<?> c) { return delegate.retainAll(c); }
        @Override public boolean removeAll(Collection<?> c) { return delegate.removeAll(c); }
        @Override public void clear() { delegate.clear(); }
    }

    /**
     * InstrumentedSet counts additions via composition/forwarding, avoiding the
     * double-counting bug that plagues the inheritance-based approach in Effective Java.
     */
    public static class InstrumentedSet<E> extends ForwardingSet<E> {
        private int addCount = 0;

        public InstrumentedSet(Set<E> delegate) {
            super(delegate);
        }

        @Override
        public boolean add(E e) {
            addCount++;
            return super.add(e);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            addCount += c.size();
            return super.addAll(c);
        }

        public int getAddCount() {
            return addCount;
        }
    }
}
