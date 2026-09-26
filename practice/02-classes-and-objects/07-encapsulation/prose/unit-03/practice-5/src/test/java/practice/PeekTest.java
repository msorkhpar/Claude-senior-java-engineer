package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("unused")
class PeekTest {

    static class Account {
        private final String accountNumber = "123456";
        private double balance = 1000.0;
    }

    static class Savings extends Account {
        private double rate = 0.02;
    }

    @Test
    void readsAPrivateField() {
        Account account = new Account();
        assertThat(Peek.read(account, "balance")).isEqualTo(1000.0);
        assertThat(Peek.read(account, "accountNumber")).isEqualTo("123456");
    }

    @Test
    void findsAFieldDeclaredInASuperclass() {
        Savings savings = new Savings();
        assertThat(Peek.read(savings, "rate")).isEqualTo(0.02);
        assertThat(Peek.read(savings, "balance")).isEqualTo(1000.0);
    }

    @Test
    void anUnknownFieldIsRefused() {
        assertThatThrownBy(() -> Peek.read(new Savings(), "owner"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
