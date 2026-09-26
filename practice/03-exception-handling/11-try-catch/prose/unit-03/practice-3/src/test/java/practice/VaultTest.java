package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VaultTest {

    @Test
    void withdrawsAndReleasesTheLock() {
        Vault vault = new Vault(100);
        assertThat(vault.withdraw(30)).isEqualTo(70);
        assertThat(vault.balance()).isEqualTo(70);
        assertThat(vault.isLocked()).isFalse();
        assertThat(vault.withdraw(70)).isZero();
        assertThat(vault.isLocked()).isFalse();
    }

    @Test
    void aRefusedWithdrawalReleasesTheLock() {
        Vault vault = new Vault(100);
        assertThatThrownBy(() -> vault.withdraw(500))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("insufficient funds");
        assertThat(vault.isLocked()).isFalse();
        assertThatThrownBy(() -> vault.withdraw(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be positive");
        assertThat(vault.isLocked()).isFalse();
    }

    @Test
    void aRefusedWithdrawalLeavesTheBalance() {
        Vault vault = new Vault(100);
        assertThatThrownBy(() -> vault.withdraw(500))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("insufficient funds");
        assertThat(vault.balance()).isEqualTo(100);
        assertThat(vault.withdraw(100)).isZero();
    }
}
