package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import practice.Shipping.Box;
import practice.Shipping.Insured;
import practice.Shipping.Letter;
import practice.Shipping.Manifest;
import practice.Shipping.Parcel;

class ShippingTest {

    /** Written by the freight team after Manifest shipped: insured, and slow. */
    record Crate(long baseCents, long declaredValueCents) implements Insured {
        public int transitDays() {
            return 6;
        }
    }

    /** Written by the courier team after Manifest shipped: not insured, next day. */
    record CourierEnvelope(long baseCents) implements Parcel {
        public int transitDays() {
            return 1;
        }
    }

    private final Manifest manifest = new Manifest();
    private final LocalDate monday = LocalDate.of(2025, 3, 3);

    @Test
    void pricesLettersAndInsuredBoxes() {
        // letter of 45 g: 85 + 2 * 10 = 105; box of 2,300 g: 450 + 2 * 120 = 690,
        // insured for 123.45: 2 % is 246.9 cents, rounded up to 247
        assertThat(manifest.totalCents(List.of(new Letter(45), new Box(2_300, 12_345)))).isEqualTo(105 + 690 + 247);
        // insured for exactly 50.00: 100 cents, nothing to round
        assertThat(manifest.totalCents(List.of(new Box(999, 5_000)))).isEqualTo(450 + 100);
        assertThat(manifest.totalCents(List.of())).isZero();
    }

    @Test
    void datesLettersAndBoxesByTheSlowestParcel() {
        assertThat(manifest.deliveredBy(monday, List.of(new Letter(10)))).isEqualTo(LocalDate.of(2025, 3, 5));
        assertThat(manifest.deliveredBy(monday, List.of(new Letter(10), new Box(500, 0))))
                .isEqualTo(LocalDate.of(2025, 3, 7));
        assertThat(manifest.deliveredBy(monday, List.of())).isEqualTo(monday);
    }

    @Test
    void anInsuredKindWrittenLaterPaysItsInsurance() {
        // crate: base 2,000, insured for 300.01 -> 600.02 cents, rounded up to 601
        assertThat(manifest.totalCents(List.of(new Crate(2_000, 30_001), new Letter(0))))
                .isEqualTo(2_000 + 601 + 85);
    }

    @Test
    void aKindWrittenLaterIsDatedByItsOwnTransitTime() {
        assertThat(manifest.deliveredBy(monday, List.of(new Letter(10), new Crate(2_000, 0))))
                .isEqualTo(LocalDate.of(2025, 3, 9));
        assertThat(manifest.deliveredBy(monday, List.of(new CourierEnvelope(900))))
                .isEqualTo(LocalDate.of(2025, 3, 4));
    }

    @Test
    void anUninsuredKindWrittenLaterPaysItsBasePostageOnly() {
        assertThat(manifest.totalCents(List.of(new CourierEnvelope(900), new Box(0, 0)))).isEqualTo(900 + 450);
    }
}
