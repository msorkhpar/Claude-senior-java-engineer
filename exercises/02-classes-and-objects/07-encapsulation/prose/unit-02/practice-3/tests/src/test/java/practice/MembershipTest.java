package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MembershipTest {

    @Test
    void readsBackItsValues() {
        Membership membership = new Membership("M-7", new Date(1_000L));
        assertThat(membership.getMemberId()).isEqualTo("M-7");
        assertThat(membership.getSince().getTime()).isEqualTo(1_000L);
    }

    @Test
    void changingTheReturnedDateChangesNothing() {
        Membership membership = new Membership("M-7", new Date(1_000L));
        membership.getSince().setTime(0L);
        assertThat(membership.getSince().getTime()).isEqualTo(1_000L);
    }

    @Test
    void changingTheGivenDateChangesNothing() {
        Date given = new Date(1_000L);
        Membership membership = new Membership("M-7", given);
        given.setTime(5L);
        assertThat(membership.getSince().getTime()).isEqualTo(1_000L);
    }

    @Test
    void theIdIsReadOnly() throws Exception {
        Membership membership = new Membership("M-9", new Date(0L));
        assertThat(membership.getMemberId()).isEqualTo("M-9");
        assertThat(Arrays.stream(Membership.class.getMethods()).map(Method::getName))
                .noneMatch(name -> name.startsWith("set"));
        for (Field field : Membership.class.getDeclaredFields()) {
            if (field.getType() == String.class) {
                assertThat(Modifier.isFinal(field.getModifiers())).as("the id field is final").isTrue();
            }
        }
    }
}
