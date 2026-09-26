package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ObservableProperty<T> {

    @FunctionalInterface
    public interface PropertyChangeListener<T> {
        void onChange(T oldValue, T newValue);
    }

    private final List<PropertyChangeListener<T>> listeners = new ArrayList<>();
    private T value;

    public ObservableProperty(T initialValue) {
        this.value = initialValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        T oldValue = this.value;
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        this.value = newValue;
        for (PropertyChangeListener<T> listener : new ArrayList<>(listeners)) {
            listener.onChange(oldValue, newValue);
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

    public int listenerCount() {
        return listeners.size();
    }
}
