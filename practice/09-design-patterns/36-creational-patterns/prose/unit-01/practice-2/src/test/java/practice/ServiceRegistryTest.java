package practice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceRegistryTest {

    @Test
    @Order(1)
    void usingTheClassDoesNotCreateTheInstance() {
        assertThat(ServiceRegistry.describe()).isEqualTo("service-registry");
        assertThat(ServiceRegistry.created()).isZero();

        ServiceRegistry.getInstance();

        assertThat(ServiceRegistry.created()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void returnsOneInstance() {
        ServiceRegistry first = ServiceRegistry.getInstance();
        ServiceRegistry second = ServiceRegistry.getInstance();

        assertThat(second).isSameAs(first);
        assertThat(ServiceRegistry.created()).isEqualTo(1);
    }

    @Test
    @Order(3)
    void aReflectiveSecondInstanceIsRefused() throws Exception {
        ServiceRegistry.getInstance();
        Constructor<ServiceRegistry> constructor = ServiceRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
        assertThat(ServiceRegistry.created()).isEqualTo(1);
    }

    @Test
    @Order(4)
    void theInstanceLivesInAPrivateNestedHolder() {
        boolean outerHoldsIt = Arrays.stream(ServiceRegistry.class.getDeclaredFields())
                .anyMatch(f -> Modifier.isStatic(f.getModifiers()) && f.getType() == ServiceRegistry.class);
        boolean holderHoldsIt = Arrays.stream(ServiceRegistry.class.getDeclaredClasses())
                .filter(c -> Modifier.isPrivate(c.getModifiers()) && Modifier.isStatic(c.getModifiers()))
                .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                .anyMatch(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                        && f.getType() == ServiceRegistry.class);

        assertThat(outerHoldsIt).as("a static ServiceRegistry field on the outer class").isFalse();
        assertThat(holderHoldsIt).as("a static final ServiceRegistry field in a private static nested class").isTrue();
    }
}
