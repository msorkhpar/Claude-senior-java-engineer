package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ParcelRunTest {

    private final ParcelRun run = new ParcelRun();

    private static ParcelRun.Parcel parcel(String id, int floor) {
        return new ParcelRun.Parcel(id, floor);
    }

    @Test
    void groupsParcelsByFloorInFloorOrder() {
        ParcelRun.Parcel a = parcel("PX-301", 4);
        ParcelRun.Parcel b = parcel("PX-302", 2);
        ParcelRun.Parcel c = parcel("PX-303", 4);
        ParcelRun.Parcel d = parcel("PX-304", 11);

        Map<Integer, List<ParcelRun.Parcel>> floors = run.byFloor(List.of(a, b, c, d));

        assertThat(floors.keySet()).containsExactly(2, 4, 11);
        assertThat(floors.get(4)).containsExactly(a, c);
        assertThat(floors.get(2)).containsExactly(b);
        assertThat(floors.get(11)).containsExactly(d);
    }

    @Test
    void aSingleParcelGetsItsSlip() {
        List<StringBuilder> slips = run.slips(List.of(parcel("QR-88", 3)));

        assertThat(slips).hasSize(1);
        assertThat(slips.get(0).toString()).isEqualTo("deliver QR-88");
    }

    @Test
    void basementFloorsAreGroupedToo() {
        ParcelRun.Parcel garage = parcel("PX-410", -2);
        ParcelRun.Parcel storage = parcel("PX-411", -1);
        ParcelRun.Parcel lobby = parcel("PX-412", 0);
        ParcelRun.Parcel office = parcel("PX-413", 6);

        Map<Integer, List<ParcelRun.Parcel>> floors = run.byFloor(List.of(office, garage, lobby, storage));

        assertThat(floors.keySet()).containsExactly(-2, -1, 0, 6);
        assertThat(floors.get(-2)).containsExactly(garage);
        assertThat(floors.get(-1)).containsExactly(storage);
    }

    @Test
    void theDisplayGetsAStringArray() {
        Object ids = run.displayIds(List.of(parcel("ZZ-1", 5), parcel("ZZ-2", 1)));

        assertThat(ids).isInstanceOf(String[].class);
        assertThat((String[]) ids).containsExactly("ZZ-1", "ZZ-2");
    }

    @Test
    void signingOneSlipLeavesTheOthersAlone() {
        List<StringBuilder> slips = run.slips(List.of(parcel("MK-5", 1), parcel("MK-6", 1), parcel("MK-7", 9)));

        slips.get(1).append(" / signed");

        assertThat(slips).extracting(StringBuilder::toString)
                .containsExactly("deliver MK-5", "deliver MK-6 / signed", "deliver MK-7");
    }
}
