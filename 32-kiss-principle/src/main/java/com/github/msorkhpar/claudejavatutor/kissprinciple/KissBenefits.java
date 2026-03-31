package com.github.msorkhpar.claudejavatutor.kissprinciple;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Demonstrates the benefits of the KISS principle for concurrent programming:
 * easier maintenance and debugging, and reduced risk of concurrency bugs.
 */
public class KissBenefits {

    // ==================== Easy-to-Debug Event Logger ====================

    /**
     * KISS approach: A simple thread-safe event logger using CopyOnWriteArrayList.
     * Easy to understand, debug, and maintain. The thread-safety is delegated
     * entirely to the concurrent collection.
     */
    public static class SimpleEventLogger {

        private final CopyOnWriteArrayList<String> events = new CopyOnWriteArrayList<>();

        public void log(String event) {
            events.add(event);
        }

        public List<String> getEvents() {
            return Collections.unmodifiableList(new ArrayList<>(events));
        }

        public void clear() {
            events.clear();
        }
    }

    // ==================== Thread-Safe State Machine ====================

    /**
     * KISS approach: Simple state machine using AtomicReference for thread-safe
     * state transitions. Each transition method uses compareAndSet for atomicity.
     * Easy to reason about and debug because the state transition logic is explicit.
     */
    public static class SimpleStateMachine {

        public enum State {
            IDLE, RUNNING, COMPLETED, FAILED
        }

        private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);

        public boolean start() {
            return state.compareAndSet(State.IDLE, State.RUNNING);
        }

        public boolean complete() {
            return state.compareAndSet(State.RUNNING, State.COMPLETED);
        }

        public boolean fail() {
            return state.compareAndSet(State.RUNNING, State.FAILED);
        }

        public boolean reset() {
            State current = state.get();
            if (current == State.COMPLETED || current == State.FAILED) {
                return state.compareAndSet(current, State.IDLE);
            }
            return false;
        }

        public State getState() {
            return state.get();
        }
    }

    // ==================== Simple Task Pipeline ====================

    /**
     * KISS approach: A simple sequential pipeline where each step transforms
     * the input. No complex graph execution, no parallel staging, no dependency
     * resolution -- just a straightforward list of transformations.
     */
    public static class SimpleTaskPipeline<T> {

        private final List<UnaryOperator<T>> steps = new ArrayList<>();

        public void addStep(UnaryOperator<T> step) {
            steps.add(step);
        }

        public T execute(T input) {
            T result = input;
            for (UnaryOperator<T> step : steps) {
                result = step.apply(result);
            }
            return result;
        }
    }

    // ==================== Simple Resource Manager ====================

    /**
     * KISS approach: Use Semaphore to manage a fixed number of resources.
     * The Semaphore handles all the waiting, signaling, and fairness concerns.
     * No need for custom wait/notify logic.
     */
    public static class SimpleResourceManager {

        private final Semaphore semaphore;

        public SimpleResourceManager(int maxResources) {
            if (maxResources <= 0) {
                throw new IllegalArgumentException("maxResources must be positive");
            }
            this.semaphore = new Semaphore(maxResources);
        }

        public boolean tryAcquire() throws InterruptedException {
            return semaphore.tryAcquire();
        }

        public void release() {
            semaphore.release();
        }

        public int availableResources() {
            return semaphore.availablePermits();
        }
    }

    // ==================== Simple Notification System ====================

    /**
     * KISS approach: A simple observer/notification system using a thread-safe list
     * of listeners. No complex event bus, no priority queues, no routing logic.
     */
    public static class SimpleNotifier<T> {

        private final CopyOnWriteArrayList<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

        public void addListener(Consumer<T> listener) {
            listeners.add(listener);
        }

        public void notify(T event) {
            for (Consumer<T> listener : listeners) {
                listener.accept(event);
            }
        }
    }
}
