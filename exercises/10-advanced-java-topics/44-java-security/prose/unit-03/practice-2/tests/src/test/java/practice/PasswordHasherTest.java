package practice;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {

    // Small counts keep the tests fast; production counts are far higher.
    private static final int OLD_COUNT = 1_200;
    private static final int NEW_COUNT = 3_100;

    private static char[] pw(String s) {
        return s.toCharArray();
    }

    @Test
    void acceptsTheRightPasswordAndRefusesOthers() throws GeneralSecurityException {
        PasswordHasher hasher = new PasswordHasher(NEW_COUNT, new SecureRandom());
        PasswordHasher.Stored row = hasher.hash(pw("tulip-staple-example"));

        assertThat(hasher.verify(pw("tulip-staple-example"), row)).isTrue();
        assertThat(hasher.verify(pw("tulip-staple-exampla"), row)).isFalse();
        assertThat(hasher.verify(pw("Tulip-staple-example"), row)).isFalse();
        assertThat(hasher.verify(pw(""), row)).isFalse();
    }

    @Test
    void twoUsersWithTheSamePasswordGetDifferentRows() throws GeneralSecurityException {
        PasswordHasher hasher = new PasswordHasher(NEW_COUNT, new SecureRandom());
        PasswordHasher.Stored first = hasher.hash(pw("summer-kayak-example"));
        PasswordHasher.Stored second = hasher.hash(pw("summer-kayak-example"));

        assertThat(second.hash()).isNotEqualTo(first.hash());
        assertThat(hasher.verify(pw("summer-kayak-example"), first)).isTrue();
        assertThat(hasher.verify(pw("summer-kayak-example"), second)).isTrue();
    }

    @Test
    void aRowMadeBeforeTheCountWasRaisedStillVerifies() throws GeneralSecurityException {
        PasswordHasher.Stored before = new PasswordHasher(OLD_COUNT, new SecureRandom())
                .hash(pw("violet-ladder-example"));
        PasswordHasher afterUpgrade = new PasswordHasher(NEW_COUNT, new SecureRandom());

        assertThat(afterUpgrade.verify(pw("violet-ladder-example"), before)).isTrue();
        assertThat(afterUpgrade.verify(pw("violet-ladder-exampel"), before)).isFalse();
    }

    @Test
    void theLoginServiceCanVerifyTheRowWithPbkdf2() throws GeneralSecurityException {
        PasswordHasher.Stored row = new PasswordHasher(NEW_COUNT, new SecureRandom())
                .hash(pw("granite-owl-example"));

        PBEKeySpec spec = new PBEKeySpec(pw("granite-owl-example"), row.salt(), row.iterations(), 256);
        byte[] expected = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();

        assertThat(row.iterations()).isEqualTo(NEW_COUNT);
        assertThat(row.salt()).hasSizeGreaterThanOrEqualTo(16);
        assertThat(row.hash()).isEqualTo(expected);
    }
}
