package practice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColumnCipherTest {

    private static final List<String> TAX_IDS = List.of(
            "FR-12 345 678 901", "", "NL-000-77", "ÖSTERREICH-ATU-99", "PT-" + "9".repeat(200));

    private static SecretKey key;

    @BeforeAll
    static void generateKey() throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        key = generator.generateKey();
    }

    @Test
    void opensWhatItSealed() throws GeneralSecurityException {
        ColumnCipher cipher = new ColumnCipher(key, new SecureRandom());
        for (String taxId : TAX_IDS) {
            assertThat(cipher.open(cipher.seal(taxId))).isEqualTo(taxId);
        }
    }

    @Test
    void aStoredValueChangedInTheDatabaseIsRefused() throws GeneralSecurityException {
        ColumnCipher cipher = new ColumnCipher(key, new SecureRandom());
        byte[] stored = cipher.seal("SE-556677-8899");
        for (int position : new int[] {14, 20}) {
            byte[] changed = stored.clone();
            changed[position] ^= 0x04;
            assertThatThrownBy(() -> cipher.open(changed))
                    .as("a value with byte %d changed", position)
                    .isInstanceOf(GeneralSecurityException.class);
        }
    }

    @Test
    void theSameTaxIdSealedRepeatedlyNeverSharesAnIv() throws GeneralSecurityException {
        ColumnCipher cipher = new ColumnCipher(key, new SecureRandom());
        Set<String> ivs = new HashSet<>();
        Set<String> values = new HashSet<>();
        for (int i = 0; i < 6; i++) {
            byte[] stored = cipher.seal("DK-31 41 59 26");
            ivs.add(HexFormat.of().formatHex(Arrays.copyOf(stored, 12)));
            values.add(HexFormat.of().formatHex(stored));
        }
        assertThat(ivs).hasSize(6);
        assertThat(values).hasSize(6);
    }

    @Test
    void anotherInstanceOfTheServiceOpensWhatThisOneSealed() throws GeneralSecurityException {
        ColumnCipher writer = new ColumnCipher(key, new SecureRandom());
        ColumnCipher reader = new ColumnCipher(key, new SecureRandom());
        byte[] first = writer.seal("BE-0123.456.789");
        byte[] second = writer.seal("IE-6388047V");

        assertThat(reader.open(first)).isEqualTo("BE-0123.456.789");
        assertThat(reader.open(second)).isEqualTo("IE-6388047V");
    }
}
