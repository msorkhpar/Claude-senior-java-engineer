package practice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Each test starts the shop afresh: it loads its own copy of the Pricing classes, so their static
 * fields are initialised again, in the order that test uses them.
 */
class PricingTest {

    /** Defines its own copy of every Pricing class; everything else comes from the test's loader. */
    static final class FreshShop extends ClassLoader {
        FreshShop() {
            super("fresh-shop", PricingTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null && name.startsWith("practice.Pricing") && !name.startsWith("practice.PricingTest")) {
                    try (InputStream in = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        if (in == null) throw new ClassNotFoundException(name);
                        byte[] bytes = in.readAllBytes();
                        c = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                return c != null ? c : getParent().loadClass(name);
            }
        }

        Object call(String type, String method, Object... args) throws Exception {
            Class<?> c = loadClass("practice.Pricing$" + type);
            for (var m : c.getMethods()) {
                if (m.getName().equals(method) && m.getParameterCount() == args.length) {
                    try {
                        return m.invoke(null, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause() instanceof Exception ex ? ex : new RuntimeException(e.getCause());
                    }
                }
            }
            throw new NoSuchMethodException(type + "." + method);
        }

        Object standardTariff() throws Exception {
            return loadClass("practice.Pricing$Tariff").getField("STANDARD").get(null);
        }
    }

    private static Object on(Object tariff, String method, Object... args) throws Exception {
        for (var m : tariff.getClass().getMethods()) {
            if (m.getName().equals(method) && m.getParameterCount() == args.length) return m.invoke(tariff, args);
        }
        throw new NoSuchMethodException(method);
    }

    @AfterEach
    void clearConfiguration() {
        System.clearProperty("pricing.vat");
    }

    @Test
    void pricesUseTheConfiguredVat() throws Exception {
        System.setProperty("pricing.vat", "17");
        FreshShop shop = new FreshShop();

        assertThat(shop.call("Rates", "vatPercent")).isEqualTo(17);
    }

    @Test
    void theStandardTariffChargesTheConfiguredVat() throws Exception {
        System.setProperty("pricing.vat", "9");
        FreshShop shop = new FreshShop();

        Object standard = shop.standardTariff();

        assertThat(on(standard, "vatPercent")).isEqualTo(9);
        assertThat(on(standard, "grossCents", 25_000L)).isEqualTo(27_250L);
    }

    @Test
    void aCustomerWhoSignsUpBeforeAnyPriceIsShownPaysTheConfiguredVat() throws Exception {
        System.setProperty("pricing.vat", "17");
        FreshShop shop = new FreshShop();

        Object tariff = shop.call("Rates", "defaultTariff");

        assertThat(tariff).isNotNull();
        assertThat(on(tariff, "vatPercent")).isEqualTo(17);
        assertThat(on(tariff, "grossCents", 4_000L)).isEqualTo(4_680L);
    }

    @Test
    void aCustomerWhoSignsUpAfterAPriceWasShownStartsOnTheStandardTariff() throws Exception {
        System.setProperty("pricing.vat", "17");
        FreshShop shop = new FreshShop();
        Object standard = shop.standardTariff();

        Object tariff = shop.call("Rates", "defaultTariff");

        assertThat(tariff).isNotNull().isSameAs(standard);
    }
}
