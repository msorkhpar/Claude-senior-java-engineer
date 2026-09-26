package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AccountTest {

    @Test
    void storesTheOwner() {
        Account account = new Account(" Ada ");
        assertThat(account.owner()).isEqualTo("Ada");
        assertThat(new Account("Grace").owner()).isEqualTo("Grace");
    }

    @Test
    void renameReplacesTheField() {
        Account account = new Account("Ada");
        assertThat(account.rename(" Grace ")).isEqualTo("Ada");
        assertThat(account.owner()).isEqualTo("Grace");
    }

    @Test
    void aMissingOwnerIsRefused() {
        assertThatThrownBy(() -> new Account(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Account("  ")).isInstanceOf(IllegalArgumentException.class);
        Account account = new Account("Ada");
        assertThatThrownBy(() -> account.rename("")).isInstanceOf(IllegalArgumentException.class);
        assertThat(account.owner()).isEqualTo("Ada");
    }
}
