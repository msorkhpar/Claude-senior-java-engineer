package practice;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.NoSuchFileException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class ThrowsDiffTest {

    static class CardDeclined extends IllegalArgumentException {}

    static class LedgerCorrupted extends Error {}

    static class Halt extends Throwable {}

    /** The SDK's public methods as the last release declared them. */
    interface Last {
        void charge();

        void refund() throws IOException, GeneralSecurityException;

        void capture();

        void settle();

        void voidAuthorization();

        void lookup() throws IOException;

        void export() throws FileNotFoundException;
    }

    /** The same methods in the release candidate. */
    interface Next {
        void charge() throws SQLException, TimeoutException;

        void refund() throws IOException, GeneralSecurityException;

        void capture() throws IllegalStateException, CardDeclined;

        void settle() throws LedgerCorrupted;

        void voidAuthorization() throws Halt, Throwable;

        void lookup() throws FileNotFoundException, NoSuchFileException;

        void export() throws IOException;
    }

    private static java.util.List<Class<?>> diff(String name) throws NoSuchMethodException {
        Method last = Last.class.getMethod(name);
        Method next = Next.class.getMethod(name);
        return ThrowsDiff.breaking(last, next);
    }

    @Test
    void checkedExceptionsOnAMethodThatDeclaredNoneAreReported() throws Exception {
        assertThat(diff("charge")).containsExactly(SQLException.class, TimeoutException.class);
    }

    @Test
    void anUnchangedThrowsClauseBreaksNothing() throws Exception {
        assertThat(diff("refund")).isEmpty();
    }

    @Test
    void addedRuntimeExceptionsAreNotReported() throws Exception {
        assertThat(diff("capture")).isEmpty();
    }

    @Test
    void anAddedErrorIsNotReported() throws Exception {
        assertThat(diff("settle")).isEmpty();
    }

    @Test
    void typesOutsideExceptionAreReported() throws Exception {
        assertThat(diff("voidAuthorization")).containsExactly(Halt.class, Throwable.class);
    }

    @Test
    void subclassesOfADeclaredExceptionAreNotReported() throws Exception {
        assertThat(diff("lookup")).isEmpty();
    }

    @Test
    void wideningToASuperclassIsReported() throws Exception {
        assertThat(diff("export")).containsExactly(IOException.class);
    }
}
