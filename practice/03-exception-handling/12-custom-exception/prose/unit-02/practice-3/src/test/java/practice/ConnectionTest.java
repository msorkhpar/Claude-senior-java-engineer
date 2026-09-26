package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ConnectionTest {

    @Test
    void sendsUntilClosed() {
        Connection c = new Connection();
        assertThat(c.send("hi")).isEqualTo("sent: hi");
        assertThat(c.send("again")).isEqualTo("sent: again");
        c.close();
        assertThatThrownBy(() -> c.send("hi"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Connection closed");
    }

    @Test
    void theTracePointsAtSend() {
        Connection c = new Connection();
        c.close();
        IllegalStateException e = catchThrowableOfType(() -> c.send("hi"), IllegalStateException.class);
        assertThat(e).isNotNull();
        StackTraceElement top = e.getStackTrace()[0];
        assertThat(top.getClassName()).isEqualTo("practice.Connection");
        assertThat(top.getMethodName()).isEqualTo("send");
    }

    @Test
    void eachFailureIsANewException() {
        Connection c = new Connection();
        c.close();
        IllegalStateException first = catchThrowableOfType(() -> c.send("a"), IllegalStateException.class);
        IllegalStateException second = catchThrowableOfType(() -> c.send("b"), IllegalStateException.class);
        assertThat(first).isNotNull();
        assertThat(second).isNotNull().isNotSameAs(first);
    }
}
