package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VaultUnlockTest {

    /** A vault that accepts one passphrase, and remembers each array it was handed and what it held. */
    static final class RecordingVault implements VaultUnlock.Vault {
        private final String accepted;
        final List<char[]> handed = new ArrayList<>();
        final List<String> read = new ArrayList<>();

        RecordingVault(String accepted) {
            this.accepted = accepted;
        }

        @Override
        public String open(char[] passphrase) {
            handed.add(passphrase);
            String seen = new String(passphrase);
            read.add(seen);
            if (!seen.equals(accepted)) throw new IllegalArgumentException("wrong passphrase");
            return "session-" + seen.length();
        }
    }

    private static char[] typed(String s) {
        return s.toCharArray();
    }

    private static boolean wiped(char[] chars) {
        for (char c : chars) if (c != '\0') return false;
        return true;
    }

    @Test
    void opensTheVaultWithThePassphraseWithoutTheNewline() {
        RecordingVault vault = new RecordingVault("blue-otter-example");

        String session = new VaultUnlock(vault).unlock(typed("blue-otter-example\n"));

        assertThat(session).isEqualTo("session-18");
        assertThat(vault.read).containsExactly("blue-otter-example");
    }

    @Test
    void opensTheVaultWhenTheConsoleAddedNoNewline() {
        RecordingVault vault = new RecordingVault("pine-cone-example");

        assertThat(new VaultUnlock(vault).unlock(typed("pine-cone-example"))).isEqualTo("session-17");
    }

    @Test
    void aWrongPassphraseIsReportedToTheCaller() {
        RecordingVault vault = new RecordingVault("blue-otter-example");

        assertThatThrownBy(() -> new VaultUnlock(vault).unlock(typed("blue-otter-exampel\n")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theOperatorsArrayIsWipedWhenThePassphraseIsWrong() {
        RecordingVault vault = new RecordingVault("blue-otter-example");
        char[] operator = typed("blue-otter-exampel\n");

        assertThatThrownBy(() -> new VaultUnlock(vault).unlock(operator)).isInstanceOf(IllegalArgumentException.class);

        assertThat(wiped(operator)).as("operator's array after a refused unlock").isTrue();
    }

    @Test
    void theArrayTheVaultReadIsWipedAfterwards() {
        RecordingVault vault = new RecordingVault("moss-lantern-example");

        new VaultUnlock(vault).unlock(typed("moss-lantern-example\n"));

        assertThat(vault.handed).hasSize(1);
        assertThat(wiped(vault.handed.get(0))).as("array the vault was handed").isTrue();
    }

    @Test
    void theOperatorsArrayIsWipedAfterASuccessfulUnlock() {
        RecordingVault vault = new RecordingVault("moss-lantern-example");
        char[] operator = typed("moss-lantern-example\n");

        new VaultUnlock(vault).unlock(operator);

        assertThat(wiped(operator)).as("operator's array after a successful unlock").isTrue();
    }
}
