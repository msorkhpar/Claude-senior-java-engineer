package com.github.msorkhpar.claudejavatutor.behavioralpatterns;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Demonstrates the Observer design pattern in Java.
 * The Observer pattern defines a one-to-many dependency between objects so that
 * when one object changes state, all its dependents are notified and updated automatically.
 */
public class ObserverPattern {

    // ===================== Classic Observer (Interface-based) =====================

    /**
     * Observer interface that receives notifications.
     */
    public interface Observer<T> {
        void update(String event, T data);
    }

    /**
     * Subject (Observable) interface that manages observers.
     */
    public interface Subject<T> {
        void addObserver(Observer<T> observer);

        void removeObserver(Observer<T> observer);

        void notifyObservers(String event, T data);
    }

    /**
     * A concrete subject representing a news agency that publishes articles.
     */
    public static class NewsAgency implements Subject<String> {
        private final List<Observer<String>> observers = new ArrayList<>();
        private final List<String> publishedArticles = new ArrayList<>();

        @Override
        public void addObserver(Observer<String> observer) {
            if (observer == null) {
                throw new IllegalArgumentException("Observer cannot be null");
            }
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }

        @Override
        public void removeObserver(Observer<String> observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers(String event, String data) {
            // Iterate over a copy to prevent ConcurrentModificationException
            List<Observer<String>> snapshot = new ArrayList<>(observers);
            for (Observer<String> observer : snapshot) {
                observer.update(event, data);
            }
        }

        public void publishArticle(String article) {
            if (article == null || article.isBlank()) {
                throw new IllegalArgumentException("Article cannot be null or blank");
            }
            publishedArticles.add(article);
            notifyObservers("NEW_ARTICLE", article);
        }

        public List<String> getPublishedArticles() {
            return Collections.unmodifiableList(publishedArticles);
        }

        public int observerCount() {
            return observers.size();
        }
    }

    /**
     * Concrete observer: a subscriber that logs received articles.
     */
    public static class NewsSubscriber implements Observer<String> {
        private final String name;
        private final List<String> receivedArticles = new ArrayList<>();

        public NewsSubscriber(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be null or blank");
            }
            this.name = name;
        }

        @Override
        public void update(String event, String data) {
            receivedArticles.add(data);
        }

        public String getName() {
            return name;
        }

        public List<String> getReceivedArticles() {
            return Collections.unmodifiableList(receivedArticles);
        }
    }

    // ===================== Event-based Observer with Multiple Event Types =====================

    /**
     * A type-safe event system supporting multiple event types with separate listener lists.
     */
    public static class EventBus {
        private final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

        public void subscribe(String eventType, Consumer<Object> listener) {
            if (eventType == null || eventType.isBlank()) {
                throw new IllegalArgumentException("Event type cannot be null or blank");
            }
            if (listener == null) {
                throw new IllegalArgumentException("Listener cannot be null");
            }
            listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        }

        public void unsubscribe(String eventType, Consumer<Object> listener) {
            List<Consumer<Object>> list = listeners.get(eventType);
            if (list != null) {
                list.remove(listener);
            }
        }

        public void publish(String eventType, Object data) {
            if (eventType == null || eventType.isBlank()) {
                throw new IllegalArgumentException("Event type cannot be null or blank");
            }
            List<Consumer<Object>> list = listeners.get(eventType);
            if (list != null) {
                for (Consumer<Object> listener : list) {
                    listener.accept(data);
                }
            }
        }

        public int listenerCount(String eventType) {
            List<Consumer<Object>> list = listeners.get(eventType);
            return list == null ? 0 : list.size();
        }

        public Set<String> eventTypes() {
            return Collections.unmodifiableSet(listeners.keySet());
        }
    }

    // ===================== Thread-safe Observer (Concurrent) =====================

    /**
     * Thread-safe subject using CopyOnWriteArrayList to handle concurrent modifications.
     */
    public static class ThreadSafeSubject<T> implements Subject<T> {
        private final CopyOnWriteArrayList<Observer<T>> observers = new CopyOnWriteArrayList<>();

        @Override
        public void addObserver(Observer<T> observer) {
            if (observer == null) {
                throw new IllegalArgumentException("Observer cannot be null");
            }
            observers.addIfAbsent(observer);
        }

        @Override
        public void removeObserver(Observer<T> observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers(String event, T data) {
            for (Observer<T> observer : observers) {
                observer.update(event, data);
            }
        }

        public int observerCount() {
            return observers.size();
        }
    }

    // ===================== Property Change Observer =====================

    /**
     * Demonstrates the property change observer pattern commonly used in JavaBeans.
     */
    public static class ObservableProperty<T> {
        private T value;
        private final List<PropertyChangeListener<T>> listeners = new ArrayList<>();

        public ObservableProperty(T initialValue) {
            this.value = initialValue;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T newValue) {
            T oldValue = this.value;
            if (!Objects.equals(oldValue, newValue)) {
                this.value = newValue;
                firePropertyChange(oldValue, newValue);
            }
        }

        public void addListener(PropertyChangeListener<T> listener) {
            if (listener == null) {
                throw new IllegalArgumentException("Listener cannot be null");
            }
            listeners.add(listener);
        }

        public void removeListener(PropertyChangeListener<T> listener) {
            listeners.remove(listener);
        }

        private void firePropertyChange(T oldValue, T newValue) {
            for (PropertyChangeListener<T> listener : new ArrayList<>(listeners)) {
                listener.onChange(oldValue, newValue);
            }
        }

        public int listenerCount() {
            return listeners.size();
        }
    }

    @FunctionalInterface
    public interface PropertyChangeListener<T> {
        void onChange(T oldValue, T newValue);
    }

    // ===================== Observer with Filtering =====================

    /**
     * An observer that only receives events matching a filter.
     */
    public static class FilteredObserver<T> implements Observer<T> {
        private final Observer<T> delegate;
        private final java.util.function.Predicate<String> eventFilter;
        private int receivedCount = 0;

        public FilteredObserver(Observer<T> delegate, java.util.function.Predicate<String> eventFilter) {
            if (delegate == null) {
                throw new IllegalArgumentException("Delegate observer cannot be null");
            }
            if (eventFilter == null) {
                throw new IllegalArgumentException("Event filter cannot be null");
            }
            this.delegate = delegate;
            this.eventFilter = eventFilter;
        }

        @Override
        public void update(String event, T data) {
            if (eventFilter.test(event)) {
                receivedCount++;
                delegate.update(event, data);
            }
        }

        public int getReceivedCount() {
            return receivedCount;
        }
    }
}
