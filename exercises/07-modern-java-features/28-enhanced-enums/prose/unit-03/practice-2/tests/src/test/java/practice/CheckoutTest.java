package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import practice.Checkout.FeeRule;
import practice.Checkout.NegotiatedRate;
import practice.Checkout.PaymentMethod;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutTest {

    @Test
    void picksTheCheapestStandardMethodForEachAmount() {
        List<PaymentMethod> all = List.of(PaymentMethod.values());

        assertThat(Checkout.cheapest(all, 950)).isEqualTo(PaymentMethod.WALLET);        // 25+22, 40, 29
        assertThat(Checkout.cheapest(all, 64_000)).isEqualTo(PaymentMethod.BANK_TRANSFER);
    }

    @Test
    void theOrderTheMethodsAreOfferedInDoesNotChangeTheAnswer() {
        List<PaymentMethod> reversed = List.of(PaymentMethod.WALLET, PaymentMethod.BANK_TRANSFER, PaymentMethod.CARD);

        assertThat(Checkout.cheapest(reversed, 950)).isEqualTo(PaymentMethod.WALLET);
        assertThat(Checkout.cheapest(reversed, 64_000)).isEqualTo(PaymentMethod.BANK_TRANSFER);
    }

    @Test
    void choosesOnlyAmongTheMethodsTheMerchantOffers() {
        // a merchant without bank transfer: at 64,000 cents card costs 1,561 and wallet 1,984
        assertThat(Checkout.cheapest(List.of(PaymentMethod.WALLET, PaymentMethod.CARD), 64_000))
                .isEqualTo(PaymentMethod.CARD);
    }

    @Test
    void aNegotiatedRateCompetesWithTheStandardMethods() {
        NegotiatedRate contract = new NegotiatedRate("card, Harbor Books rate", 10, 150);
        List<FeeRule> offered = List.of(PaymentMethod.CARD, contract, PaymentMethod.WALLET);

        assertThat(Checkout.cheapest(offered, 7_300)).isEqualTo(contract);               // 200, 119, 226
    }
}
