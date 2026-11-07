package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogueLoaderTest {

    private static final String[] CURRENCIES = {"SEK", "NOK", "DKK"};
    private static final String[] COUNTRIES = {"SE", "NO"};

    private static List<String> priceList(int products) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < products; i++) {
            lines.add("P" + (70_000 + i) + ";Knäckebröd pack " + i + ";"
                    + CURRENCIES[i % 3] + ";" + COUNTRIES[i % 2]);
        }
        return lines;
    }

    @Test
    void everyLineBecomesAProductWithItsFields() {
        List<CatalogueLoader.Product> products = new CatalogueLoader().load(List.of(
                "P31207;Lingonberry jam 400 g;SEK;SE",
                "P31208;Brunost 500 g;NOK;NO"));

        assertThat(products).containsExactly(
                new CatalogueLoader.Product("P31207", "Lingonberry jam 400 g", "SEK", "SE"),
                new CatalogueLoader.Product("P31208", "Brunost 500 g", "NOK", "NO"));
    }

    @Test
    void productsWithTheSameCurrencyAndCountryHoldOneStringEach() {
        List<CatalogueLoader.Product> products = new CatalogueLoader().load(priceList(12));

        CatalogueLoader.Product first = products.get(0);
        CatalogueLoader.Product seventh = products.get(6);    // the same currency and country again
        assertThat(seventh.currency()).isEqualTo(first.currency()).isSameAs(first.currency());
        assertThat(seventh.country()).isEqualTo(first.country()).isSameAs(first.country());
    }

    @Test
    void theTableHoldsTheRepeatedValuesOnly() {
        CatalogueLoader loader = new CatalogueLoader();
        loader.load(priceList(600));

        assertThat(loader.tableSize()).isEqualTo(CURRENCIES.length + COUNTRIES.length);
    }
}
