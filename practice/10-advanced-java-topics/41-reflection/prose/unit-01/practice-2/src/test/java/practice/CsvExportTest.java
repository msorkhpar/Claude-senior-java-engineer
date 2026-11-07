package practice;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExportTest {

    record Parcel(String trackingId, int grams, String destination) {}

    sealed interface Notice permits Email, Sms {}
    record Email(String to, String subject) implements Notice {}
    record Sms(String phone, String text) implements Notice {}

    sealed interface Delivery permits Courier, Pickup {}
    record Courier(String route, int stops) implements Delivery {}
    sealed interface Pickup extends Delivery permits StorePickup, Locker {}
    record StorePickup(String store) implements Pickup {}
    record Locker(String lockerId, int slot) implements Pickup {}

    private static Map<String, Object> byColumn(Record r) {
        List<String> columns = CsvExport.columns(r.getClass());
        List<Object> values = CsvExport.values(r);
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) m.put(columns.get(i), values.get(i));
        return m;
    }

    @Test
    void eachValueSitsUnderItsOwnColumn() {
        Map<String, Object> row = byColumn(new Parcel("TRK-88", 1250, "Lyon"));

        assertThat(row).containsEntry("trackingId", "TRK-88")
                .containsEntry("grams", 1250)
                .containsEntry("destination", "Lyon");
    }

    @Test
    void aFamilyExportHasOneSectionPerRecordTypeEvenWhenItIsEmpty() {
        String csv = CsvExport.exportFamily(Notice.class, List.of(new Email("ops@example.com", "Weekly")));

        assertThat(CsvExport.recordTypes(Notice.class)).containsExactlyInAnyOrder(Email.class, Sms.class);
        assertThat(csv).contains("## Email\n").contains("## Sms\n").contains("ops@example.com").contains("Weekly");
    }

    @Test
    void theColumnsAreTheRecordsValuesAndNothingElse() {
        assertThat(CsvExport.columns(Parcel.class)).containsExactlyInAnyOrder("trackingId", "grams", "destination");
    }

    @Test
    void theColumnsFollowTheOrderTheRecordDeclaresThem() {
        String csv = CsvExport.export(Parcel.class, List.of(new Parcel("TRK-12", 300, "Graz")));

        assertThat(csv.lines().findFirst()).contains("trackingId,grams,destination");
    }

    @Test
    void theCasesOfANestedSealedInterfaceAreIncluded() {
        assertThat(CsvExport.recordTypes(Delivery.class))
                .containsExactlyInAnyOrder(Courier.class, StorePickup.class, Locker.class);
    }

    @Test
    void aNestedSealedInterfaceIsNotACaseOfItsOwn() {
        assertThat(CsvExport.recordTypes(Delivery.class)).extracting(Class::getSimpleName)
                .doesNotContain(Pickup.class.getSimpleName());
    }
}
