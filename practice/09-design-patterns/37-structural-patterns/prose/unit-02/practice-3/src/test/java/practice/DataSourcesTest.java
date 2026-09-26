package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DataSourcesTest {

    @Test
    void layersRoundTripAndOrderDecidesTheStoredForm() {
        DataSources.InMemoryDataSource store1 = new DataSources.InMemoryDataSource();
        DataSources.DataSource compressThenEncrypt =
                new DataSources.CompressionDecorator(new DataSources.EncryptionDecorator(store1, 3));
        compressThenEncrypt.write("aaabbcc");

        DataSources.InMemoryDataSource store2 = new DataSources.InMemoryDataSource();
        DataSources.DataSource encryptThenCompress =
                new DataSources.EncryptionDecorator(new DataSources.CompressionDecorator(store2), 3);
        encryptThenCompress.write("aaabbcc");

        assertThat(store1.read()).isEqualTo("d6e5f5");
        assertThat(compressThenEncrypt.read()).isEqualTo("aaabbcc");
        assertThat(store2.read()).isEqualTo("d3e2f2");
        assertThat(encryptThenCompress.read()).isEqualTo("aaabbcc");
    }

    @Test
    void aRunOfOneHasNoCount() {
        DataSources.InMemoryDataSource store = new DataSources.InMemoryDataSource();
        DataSources.DataSource compressed = new DataSources.CompressionDecorator(store);

        compressed.write("abc");
        assertThat(store.read()).isEqualTo("abc");

        compressed.write("abbc");
        assertThat(store.read()).isEqualTo("ab2c");
    }

    @Test
    void aLongRunKeepsAllItsDigits() {
        DataSources.InMemoryDataSource store = new DataSources.InMemoryDataSource();
        DataSources.DataSource compressed = new DataSources.CompressionDecorator(store);

        compressed.write("a".repeat(12) + "b");

        assertThat(store.read()).isEqualTo("a12b");
        assertThat(compressed.read()).isEqualTo("a".repeat(12) + "b");
    }

    @Test
    void dataWithDigitsIsRefused() {
        DataSources.InMemoryDataSource store = new DataSources.InMemoryDataSource();
        DataSources.DataSource compressed = new DataSources.CompressionDecorator(store);

        assertThatIllegalArgumentException().isThrownBy(() -> compressed.write("a2"));
        assertThatIllegalArgumentException().isThrownBy(() -> compressed.write("7"));
        assertThat(store.read()).isEmpty();
    }

    @Test
    void readsGoThroughTheWrappedSource() {
        DataSources.InMemoryDataSource store = new DataSources.InMemoryDataSource();
        DataSources.DataSource compressed = new DataSources.CompressionDecorator(store);

        compressed.write("xx");
        store.write("y3");

        assertThat(compressed.read()).isEqualTo("yyy");
    }
}
