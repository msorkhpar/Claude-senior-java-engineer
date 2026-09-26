package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CafeTest {

    @Test
    void pricesEachKindOfOrder() {
        assertThat(Cafe.priceCents(new Cafe.Drink(Cafe.Size.SMALL, 1))).isEqualTo(300);
        assertThat(Cafe.priceCents(new Cafe.Drink(Cafe.Size.MEDIUM, 3))).isEqualTo(450);
        assertThat(Cafe.priceCents(new Cafe.Pastry("croissant", 2))).isEqualTo(500);
        assertThat(Cafe.priceCents(new Cafe.GiftCard(2000))).isEqualTo(2000);
    }

    @Test
    void aLargeDrinkUsesItsOwnBasePrice() {
        assertThat(Cafe.Size.LARGE.baseCents()).isEqualTo(425);
        assertThat(Cafe.priceCents(new Cafe.Drink(Cafe.Size.LARGE, 1))).isEqualTo(425);
    }

    @Test
    void onlyExtraShotsCost() {
        assertThat(Cafe.priceCents(new Cafe.Drink(Cafe.Size.SMALL, 0))).isEqualTo(300);
        assertThat(Cafe.priceCents(new Cafe.Drink(Cafe.Size.SMALL, 2))).isEqualTo(350);
    }
}
