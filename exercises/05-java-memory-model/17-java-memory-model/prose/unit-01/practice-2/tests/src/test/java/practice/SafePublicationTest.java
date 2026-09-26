package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SafePublicationTest {

    private static List<Field> instanceFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && !f.isSynthetic()) {
                fields.add(f);
            }
        }
        return fields;
    }

    @Test
    void publishesAndReadsBack() {
        SafePublication.Point p = new SafePublication.Point(1, 2);
        SafePublication.Point q = p.moved(3, 4);
        assertThat(q.x()).isEqualTo(4);
        assertThat(q.y()).isEqualTo(6);
        assertThat(p.x()).isEqualTo(1);
        assertThat(p.y()).isEqualTo(2);

        SafePublication.Publisher publisher = new SafePublication.Publisher();
        assertThat(publisher.current()).isNull();
        SafePublication.Polygon polygon = new SafePublication.Polygon(
                List.of(new SafePublication.Point(0, 0), new SafePublication.Point(1, 1)));
        publisher.publish(polygon);
        assertThat(publisher.current()).isSameAs(polygon);
        assertThat(publisher.current().points()).extracting(SafePublication.Point::x).containsExactly(0, 1);
        assertThat(publisher.current().points()).extracting(SafePublication.Point::y).containsExactly(0, 1);
    }

    @Test
    void pointFieldsAreFinal() {
        SafePublication.Point p = new SafePublication.Point(3, 4);
        assertThat(p.x()).isEqualTo(3);
        new SafePublication.Polygon(List.of(p));
        List<Field> fields = new ArrayList<>(instanceFields(SafePublication.Point.class));
        fields.addAll(instanceFields(SafePublication.Polygon.class));
        assertThat(fields).isNotEmpty();
        assertThat(fields).allSatisfy(f -> {
            assertThat(Modifier.isFinal(f.getModifiers())).as(f.getName() + " is final").isTrue();
            assertThat(Modifier.isPrivate(f.getModifiers())).as(f.getName() + " is private").isTrue();
        });
    }

    @Test
    void aPolygonCannotBeChangedFromOutside() {
        List<SafePublication.Point> mine = new ArrayList<>();
        mine.add(new SafePublication.Point(0, 0));
        SafePublication.Polygon polygon = new SafePublication.Polygon(mine);
        mine.add(new SafePublication.Point(5, 5));
        assertThat(polygon.points()).hasSize(1);
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> polygon.points().add(new SafePublication.Point(9, 9)));
        assertThat(polygon.points()).hasSize(1);
    }

    @Test
    void thePublishedReferenceIsVolatile() {
        SafePublication.Publisher publisher = new SafePublication.Publisher();
        SafePublication.Polygon polygon = new SafePublication.Polygon(List.of(new SafePublication.Point(2, 2)));
        publisher.publish(polygon);
        assertThat(publisher.current()).isSameAs(polygon);
        List<Field> holders = instanceFields(SafePublication.Publisher.class).stream()
                .filter(f -> f.getType() == SafePublication.Polygon.class)
                .toList();
        assertThat(holders).isNotEmpty();
        assertThat(holders).allSatisfy(f ->
                assertThat(Modifier.isVolatile(f.getModifiers())).as(f.getName() + " is volatile").isTrue());
    }
}
