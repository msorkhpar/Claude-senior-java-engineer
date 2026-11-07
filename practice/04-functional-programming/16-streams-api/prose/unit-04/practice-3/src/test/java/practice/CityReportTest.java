package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CityReportTest {

    private static CityReport.Sale sale(String city, String customer, long cents) {
        return new CityReport.Sale(city, customer, cents);
    }

    /** A busy day: every customer buys once, in one city; some sales are large. */
    private static final List<CityReport.Sale> BUSY = List.of(
            sale("lyon", "k-101", 2_400), sale("graz", "k-202", 350), sale("lyon", "k-103", 990),
            sale("porto", "k-304", 5_100), sale("graz", "k-205", 1_250), sale("porto", "k-306", 75));

    @Test
    void aBusyDayIsSummedPerCityAndSplitBySize() {
        Map<String, Long> revenue = CityReport.revenueByCity(BUSY);
        assertThat(revenue).containsOnly(Map.entry("lyon", 3_390L), Map.entry("graz", 1_600L), Map.entry("porto", 5_175L));

        Map<String, Integer> customers = CityReport.customersByCity(BUSY);
        assertThat(customers).containsOnly(Map.entry("lyon", 2), Map.entry("graz", 2), Map.entry("porto", 2));

        Map<Boolean, List<CityReport.Sale>> split = CityReport.splitBySize(BUSY, 1_000);
        assertThat(split.get(true)).extracting(CityReport.Sale::customer).containsExactlyInAnyOrder("k-101", "k-304", "k-205");
        assertThat(split.get(false)).extracting(CityReport.Sale::customer).containsExactlyInAnyOrder("k-202", "k-103", "k-306");
    }

    @Test
    void citiesComeOutInAlphabeticalOrder() {
        List<CityReport.Sale> sales = new ArrayList<>(BUSY);
        sales.add(sale("turku", "k-407", 640));
        sales.add(sale("bergen", "k-508", 820));

        assertThat(CityReport.revenueByCity(sales).keySet())
                .containsExactly("bergen", "graz", "lyon", "porto", "turku");
    }

    @Test
    void aQuietDayWithNoLargeSaleStillHasBothLists() {
        List<CityReport.Sale> quiet = List.of(sale("graz", "k-611", 420), sale("porto", "k-612", 180));

        Map<Boolean, List<CityReport.Sale>> split = CityReport.splitBySize(quiet, 1_000);

        assertThat(split.get(true)).isNotNull().isEmpty();
        assertThat(split.get(false)).hasSize(2);
        assertThat(CityReport.render(quiet, 1_000)).contains("large sales: 0, small sales: 2");
    }

    @Test
    void aRegularWhoComesBackCountsAsOneCustomer() {
        List<CityReport.Sale> sales = List.of(sale("lyon", "k-701", 300), sale("lyon", "k-701", 450),
                sale("lyon", "k-701", 120), sale("lyon", "k-702", 900), sale("graz", "k-701", 60));

        assertThat(CityReport.customersByCity(sales)).containsOnly(Map.entry("lyon", 2), Map.entry("graz", 1));
    }
}
