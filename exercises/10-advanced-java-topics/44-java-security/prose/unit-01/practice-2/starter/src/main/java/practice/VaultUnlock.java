package practice;

import java.util.Arrays;

/** Opens the deployment vault with the passphrase an operator types at the console. */
public class VaultUnlock {

    /** The vault's client library. It reads the passphrase during the call and keeps no reference. */
    public interface Vault {
        /** Returns a session id; throws IllegalArgumentException when the passphrase is wrong. */
        String open(char[] passphrase);
    }

    private final Vault vault;

    public VaultUnlock(Vault vault) {
        this.vault = vault;
    }

    /** How many characters of typed are the passphrase: the console may add a trailing newline. */
    static int passphraseLength(char[] typed) {
        int length = typed.length;
        if (length > 0 && typed[length - 1] == '\n') length--;
        return length;
    }

    /** Opens the vault with what the operator typed, without its trailing newline; returns the session id. */
    public String unlock(char[] typed) {
        // TODO: Hand the vault the passphrase without the console's newline, and return the
        //       session id. Once this method returns or throws, the passphrase must be gone
        //       from memory that this class can reach.
        throw new IllegalStateException("TODO: unlock");
    }
}
