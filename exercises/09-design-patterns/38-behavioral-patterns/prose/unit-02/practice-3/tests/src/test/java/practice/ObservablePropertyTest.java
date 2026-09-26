package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import practice.ObservableProperty.PropertyChangeListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservablePropertyTest {

    private static PropertyChangeListener<String> recordingInto(List<String> log) {
        return (oldValue, newValue) -> log.add(oldValue + "->" + newValue);
    }

    @Test
    void aChangeReachesEveryListenerWithOldAndNew() {
        ObservableProperty<String> colour = new ObservableProperty<>("red");
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        PropertyChangeListener<String> secondListener = recordingInto(second);
        colour.addListener(recordingInto(first));
        colour.addListener(secondListener);

        colour.setValue("blue");
        colour.removeListener(secondListener);
        colour.setValue("green");

        assertThat(colour.getValue()).isEqualTo("green");
        assertThat(first).containsExactly("red->blue", "blue->green");
        assertThat(second).containsExactly("red->blue");
        assertThat(colour.listenerCount()).isEqualTo(1);
        assertThatThrownBy(() -> colour.addListener(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anEqualValueIsNoChange() {
        ObservableProperty<String> colour = new ObservableProperty<>(new String("blue"));
        List<String> log = new ArrayList<>();
        colour.addListener(recordingInto(log));

        colour.setValue(new StringBuilder("bl").append("ue").toString());

        assertThat(log).isEmpty();
    }

    @Test
    void nullIsAnOrdinaryValue() {
        ObservableProperty<String> name = new ObservableProperty<>(null);
        List<String> log = new ArrayList<>();
        name.addListener(recordingInto(log));

        name.setValue("x");
        name.setValue(null);
        name.setValue(null);

        assertThat(log).containsExactly("null->x", "x->null");
        assertThat(name.getValue()).isNull();
    }

    @Test
    void listenersReadTheNewValue() {
        ObservableProperty<Integer> temperature = new ObservableProperty<>(1000);
        List<Integer> pulled = new ArrayList<>();
        temperature.addListener((oldValue, newValue) -> pulled.add(temperature.getValue()));

        temperature.setValue(2000);

        assertThat(pulled).containsExactly(2000);
    }
}
