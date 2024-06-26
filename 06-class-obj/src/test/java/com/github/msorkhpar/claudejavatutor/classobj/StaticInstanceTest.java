package com.github.msorkhpar.claudejavatutor.classobj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;


class StaticInstanceTest {

    @AfterEach
    void resetStatic() throws NoSuchFieldException, IllegalAccessException {
        // This is a very bad test and should not become a habit! When a test involves a static variable there should
        // be a way to mock it instead.
        Field field = StaticInstance.class.getDeclaredField("staticCounter");
        field.setAccessible(true);
        field.set(null, 10);
    }

    @Test
    void testStaticCounter() {
        StaticInstance.incrementStaticCounter();
        assertThat(StaticInstance.getStaticCounter()).isEqualTo(11); // 10 from static initializer + 1

        StaticInstance.incrementStaticCounter();
        assertThat(StaticInstance.getStaticCounter()).isEqualTo(12);
    }

    @Test
    void testInstanceCounter() {
        StaticInstance instance1 = new StaticInstance();
        StaticInstance instance2 = new StaticInstance();

        instance1.incrementInstanceCounter();
        assertThat(instance1.getInstanceCounter()).isEqualTo(6); // 5 from instance initializer + 1
        assertThat(instance2.getInstanceCounter()).isEqualTo(5); // Not affected by instance1

        instance2.incrementInstanceCounter();
        instance2.incrementInstanceCounter();
        assertThat(instance2.getInstanceCounter()).isEqualTo(7);
        assertThat(instance1.getInstanceCounter()).isEqualTo(6); // Still unchanged
    }

    @Test
    void testStaticAndInstanceInteraction() {
        StaticInstance instance = new StaticInstance();

        StaticInstance.incrementStaticCounter();
        instance.incrementInstanceCounter();

        assertThat(StaticInstance.getStaticCounter()).isEqualTo(11); // Shared among all instances
        assertThat(instance.getInstanceCounter()).isEqualTo(6); // Specific to this instance
    }

    @Test
    void testMultipleInstances() {
        StaticInstance instance1 = new StaticInstance();
        StaticInstance instance2 = new StaticInstance();

        instance1.incrementInstanceCounter();
        StaticInstance.incrementStaticCounter();

        assertThat(instance1.getInstanceCounter()).isEqualTo(6);
        assertThat(instance2.getInstanceCounter()).isEqualTo(5);
        assertThat(StaticInstance.getStaticCounter()).isEqualTo(11); // Shared by both instances
    }
}