package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ScheduleTest {

    @Test
    void withSlotReturnsTheLongerSchedule() {
        Schedule monday = new Schedule("Mon", List.of("09:00"));
        Schedule longer = monday.withSlot("14:00");
        assertThat(longer.getName()).isEqualTo("Mon");
        assertThat(longer.getSlots()).containsExactly("09:00", "14:00");
    }

    @Test
    void theOriginalNeverChanges() {
        Schedule monday = new Schedule("Mon", List.of("09:00"));
        Schedule longer = monday.withSlot("14:00");
        assertThat(longer).isNotSameAs(monday);
        catchThrowable(() -> monday.getSlots().add("18:00"));
        assertThat(monday.getSlots()).containsExactly("09:00");
    }

    @Test
    void theClassAndItsFieldsAreFinal() {
        assertThat(Modifier.isFinal(Schedule.class.getModifiers())).as("Schedule is final").isTrue();
        Field[] fields = Schedule.class.getDeclaredFields();
        assertThat(fields).isNotEmpty();
        for (Field field : fields) {
            assertThat(Modifier.isPrivate(field.getModifiers())).as("%s is private", field.getName()).isTrue();
            assertThat(Modifier.isFinal(field.getModifiers())).as("%s is final", field.getName()).isTrue();
        }
    }

    @Test
    void theCallersListIsNotShared() {
        List<String> given = new ArrayList<>(List.of("09:00", "11:00"));
        Schedule monday = new Schedule("Mon", given);
        given.add("18:00");
        given.set(0, "07:00");
        assertThat(monday.getSlots()).containsExactly("09:00", "11:00");
    }
}
