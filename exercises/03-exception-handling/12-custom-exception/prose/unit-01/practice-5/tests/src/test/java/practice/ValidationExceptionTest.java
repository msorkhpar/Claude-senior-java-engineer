package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationExceptionTest {

    @Test
    void keepsTheMessageWithoutAStackTrace() {
        ValidationException e = new ValidationException("bad");
        assertThat(e.getMessage()).isEqualTo("bad");
        assertThat(e.getStackTrace()).isEmpty();
    }

    @Test
    void suppressionIsSwitchedOff() {
        ValidationException e = new ValidationException("bad");
        e.addSuppressed(new IllegalStateException("close failed"));
        assertThat(e.getSuppressed()).isEmpty();
    }

    @Test
    void aTraceCannotBeSetLater() {
        ValidationException e = new ValidationException("bad");
        e.setStackTrace(new StackTraceElement[] {new StackTraceElement("Form", "submit", "Form.java", 12)});
        assertThat(e.getStackTrace()).isEmpty();
    }

    @Test
    void theCauseFormKeepsTheCause() {
        NumberFormatException cause = new NumberFormatException("For input string: \"x\"");
        ValidationException e = new ValidationException("bad age", cause);
        assertThat(e.getMessage()).isEqualTo("bad age");
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getStackTrace()).isEmpty();
    }
}
