package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;

class ServerStatusTest {

    /** Every instance field's value, read by reflection; atomics are read as their plain value. */
    private static Map<Field, Object> values(ServerStatus status) throws IllegalAccessException {
        Map<Field, Object> values = new HashMap<>();
        for (Field field : ServerStatus.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(status);
            if (value instanceof AtomicBoolean b) {
                value = b.get();
            } else if (value instanceof AtomicInteger i) {
                value = (long) i.get();
            } else if (value instanceof AtomicLong l) {
                value = l.get();
            } else if (value instanceof LongAdder a) {
                value = a.sum();
            } else if (value instanceof Integer i) {
                value = (long) i;
            }
            values.put(field, value);
        }
        return values;
    }

    /** The fields whose value the action changed. */
    private static List<Field> changedBy(ServerStatus status, Runnable action) throws IllegalAccessException {
        Map<Field, Object> before = values(status);
        action.run();
        Map<Field, Object> after = values(status);
        List<Field> changed = new ArrayList<>();
        for (Field field : before.keySet()) {
            if (!Objects.equals(before.get(field), after.get(field))) {
                changed.add(field);
            }
        }
        return changed;
    }

    private static boolean volatileOrAtomic(Field field) {
        return Modifier.isVolatile(field.getModifiers())
                || field.getType().getPackageName().equals("java.util.concurrent.atomic");
    }

    @Test
    void tracksTheStatus() {
        ServerStatus status = new ServerStatus();
        assertThat(status.isUp()).isFalse();
        status.markUp();
        assertThat(status.isUp()).isTrue();
        status.markDown();
        assertThat(status.isUp()).isFalse();
        assertThat(status.requests()).isZero();
        status.request();
        status.request();
        status.request();
        assertThat(status.requests()).isEqualTo(3);
        assertThat(status.lastRestart()).isZero();
        status.restartedAt(1_700_000_000_123L);
        assertThat(status.lastRestart()).isEqualTo(1_700_000_000_123L);
    }

    @Test
    void theUpFlagIsVolatile() throws IllegalAccessException {
        ServerStatus status = new ServerStatus();
        List<Field> flag = changedBy(status, status::markUp);
        assertThat(flag).as("markUp writes one field").hasSize(1);
        assertThat(volatileOrAtomic(flag.get(0))).as("the up flag %s is volatile", flag.get(0).getName()).isTrue();
    }

    @Test
    void theRestartTimeIsReadWhole() throws IllegalAccessException {
        ServerStatus status = new ServerStatus();
        List<Field> time = changedBy(status, () -> status.restartedAt(1_700_000_000_123L));
        assertThat(time).as("restartedAt writes one field").hasSize(1);
        assertThat(volatileOrAtomic(time.get(0))).as("the restart time %s is volatile", time.get(0).getName()).isTrue();
    }

    @Test
    void requestCountingIsAtomic() throws Exception {
        ServerStatus status = new ServerStatus();
        List<Field> count = changedBy(status, status::request);
        assertThat(count).as("request changes one field").hasSize(1);
        boolean atomicClass = count.get(0).getType().getPackageName().equals("java.util.concurrent.atomic");
        boolean synchronizedMethod = Modifier.isSynchronized(ServerStatus.class.getMethod("request").getModifiers());
        assertThat(atomicClass || synchronizedMethod)
                .as("the count %s is updated atomically", count.get(0).getName()).isTrue();
    }
}
