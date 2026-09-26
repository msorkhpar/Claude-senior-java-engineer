package practice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WalletTest {

    private static Wallet ana() {
        return Wallet.builder().owner("Ana").cents(250).build();
    }

    @Test
    void aBuiltWalletReportsItsBalance() {
        Wallet wallet = ana();
        assertThat(wallet.balance()).isEqualTo(250);
        assertThat(wallet.auditor().report()).isEqualTo("Ana: 250");
    }

    @Test
    void theAuditorSeesTheLiveBalance() {
        Wallet wallet = ana();
        Wallet.Auditor auditor = wallet.auditor();
        wallet.deposit(100);
        assertThat(auditor.report()).isEqualTo("Ana: 350");
    }

    @Test
    void aCorrectionChangesTheWallet() {
        Wallet wallet = ana();
        wallet.auditor().correct(-50);
        assertThat(wallet.balance()).isEqualTo(200);
        assertThat(wallet.auditor().report()).isEqualTo("Ana: 200");
    }

    @Test
    void onlyTheBuilderMakesWallets() {
        Wallet wallet = Wallet.builder().owner("Ben").cents(0).build();
        assertThat(wallet.auditor().report()).isEqualTo("Ben: 0");
        for (Constructor<?> constructor : Wallet.class.getDeclaredConstructors()) {
            assertThat(Modifier.isPrivate(constructor.getModifiers()))
                    .as("every Wallet constructor is private")
                    .isTrue();
        }
    }
}
