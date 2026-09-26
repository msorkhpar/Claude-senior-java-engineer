package practice;

public final class ObservableProperty<T> {

    @FunctionalInterface
    public interface PropertyChangeListener<T> {
        void onChange(T oldValue, T newValue);
    }

    public ObservableProperty(T initialValue) {
        throw new UnsupportedOperationException("TODO");
    }

    public T getValue() {
        throw new UnsupportedOperationException("TODO");
    }

    public void setValue(T newValue) {
        throw new UnsupportedOperationException("TODO");
    }

    public void addListener(PropertyChangeListener<T> listener) {
        throw new UnsupportedOperationException("TODO");
    }

    public void removeListener(PropertyChangeListener<T> listener) {
        throw new UnsupportedOperationException("TODO");
    }

    public int listenerCount() {
        throw new UnsupportedOperationException("TODO");
    }
}
