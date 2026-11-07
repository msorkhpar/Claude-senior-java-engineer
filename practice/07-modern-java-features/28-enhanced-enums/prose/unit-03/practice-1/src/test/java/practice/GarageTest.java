package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import practice.Garage.Tariff;
import practice.Garage.Visit;

import static org.assertj.core.api.Assertions.assertThat;

/** Each tariff is one object for the whole JVM, so the order of these cases is fixed. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GarageTest {

    @Test
    @Order(1)
    void chargesEveryStartedHourOfEachPart() {
        Visit van = new Visit("KL-482-TZ");

        assertThat(Tariff.STANDARD.charge(van, 50)).isEqualTo(250);
        assertThat(Tariff.STANDARD.charge(van, 61)).isEqualTo(500);
        assertThat(Tariff.STANDARD.charge(van, 120)).isEqualTo(500);
    }

    @Test
    @Order(2)
    void stopsChargingAVisitAtItsCap() {
        Visit hatchback = new Visit("RB-17-QX");

        long total = 0;
        for (int part = 0; part < 4; part++) total += Tariff.RESIDENT.charge(hatchback, 120);

        assertThat(total).isEqualTo(Tariff.RESIDENT.capCents());
        assertThat(Tariff.RESIDENT.charge(hatchback, 45)).isZero();
    }

    @Test
    @Order(3)
    void aVisitThatStartsAfterAnotherReachedItsCapPaysInFull() {
        Visit first = new Visit("MZ-900-AA");
        Visit second = new Visit("MZ-901-AB");
        for (int part = 0; part < 5; part++) Tariff.EVENT.charge(first, 60);

        assertThat(Tariff.EVENT.charge(second, 60)).isEqualTo(600);
        assertThat(Tariff.EVENT.charge(second, 180)).isEqualTo(1_800);
    }

    @Test
    @Order(4)
    void visitsThatOverlapAreCappedSeparately() {
        Visit early = new Visit("TT-310-EE");
        Visit late = new Visit("TT-311-LL");

        long earlyTotal = 0;
        long lateTotal = 0;
        for (int part = 0; part < 6; part++) {
            earlyTotal += Tariff.STANDARD.charge(early, 120);
            lateTotal += Tariff.STANDARD.charge(late, 60);
        }

        assertThat(earlyTotal).isEqualTo(1_800);
        assertThat(lateTotal).isEqualTo(1_500);
    }
}
