package practice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import practice.SecureSingletons.Settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureSingletonsTest {

    @Test
    void getInstanceReturnsOneSettings() {
        Settings first = Settings.getInstance();

        assertThat(Settings.getInstance()).isSameAs(first);
        assertThat(first.name()).isEqualTo("settings");
    }

    @Test
    void deserializingGivesBackTheInstance() throws Exception {
        Settings original = Settings.getInstance();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        Object back;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            back = in.readObject();
        }

        assertThat(back).isSameAs(original);
    }

    @Test
    void reflectionCannotMakeASecond() throws Exception {
        Settings.getInstance();
        Constructor<Settings> constructor = Settings.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void cloningIsRefused() {
        Settings settings = Settings.getInstance();

        assertThatThrownBy(settings::clone).isInstanceOf(CloneNotSupportedException.class);
    }
}
