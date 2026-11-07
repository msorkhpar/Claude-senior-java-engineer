package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Every test loads SeatLicense afresh in a class loader of its own, so each one starts from a
 * process in which nothing has touched the license yet.
 */
class SeatLicenseTest {

    private ClassLoader loader;
    private Class<?> license;

    @BeforeEach
    void freshProcess() throws Exception {
        URL classes = SeatLicense.class.getProtectionDomain().getCodeSource().getLocation();
        loader = new URLClassLoader(new URL[] {classes}, ClassLoader.getPlatformClassLoader());
        license = Class.forName("practice.SeatLicense", false, loader);
    }

    private Object instance() throws Exception {
        return license.getMethod("getInstance").invoke(null);
    }

    private boolean claim(Object lic) throws Exception {
        return (Boolean) license.getMethod("claim").invoke(lic);
    }

    private int free(Object lic) throws Exception {
        return (Integer) license.getMethod("free").invoke(lic);
    }

    private byte[] write(Object lic) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(lic);
        }
        return bytes.toByteArray();
    }

    private Object read(byte[] bytes) throws Exception {
        InputStream in = new ByteArrayInputStream(bytes);
        try (ObjectInputStream objects = new ObjectInputStream(in) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
                return Class.forName(desc.getName(), false, loader);
            }
        }) {
            return objects.readObject();
        }
    }

    @Test
    void oneLicenseCountsEverySeat() throws Exception {
        Object first = instance();
        Object second = instance();
        assertThat(second).isSameAs(first);
        for (int i = 0; i < 25; i++) assertThat(claim(i % 2 == 0 ? first : second)).isTrue();
        assertThat(claim(first)).isFalse();
        license.getMethod("release").invoke(second);
        assertThat(free(first)).isEqualTo(1);
    }

    @Test
    void aConstructorCalledReflectivelyBeforeFirstUseIsRefused() throws Exception {
        Constructor<?> constructor = license.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class);
        Object lic = instance();
        assertThat(free(lic)).isEqualTo(25);
        assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class);
    }

    @Test
    void aLicenseReadBackFromASessionIsTheLiveOne() throws Exception {
        Object lic = instance();
        Object back = read(write(lic));
        assertThat(back).isSameAs(lic);
    }

    @Test
    void seatsClaimedAfterTheSessionWasWrittenStayClaimed() throws Exception {
        Object lic = instance();
        for (int i = 0; i < 3; i++) claim(lic);
        byte[] saved = write(lic);
        for (int i = 0; i < 4; i++) claim(lic);
        Object back = read(saved);
        assertThat(free(back)).isEqualTo(18);
        assertThat(free(lic)).isEqualTo(18);
    }
}
