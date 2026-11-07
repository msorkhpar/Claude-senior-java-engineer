package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import practice.Payouts.AuditTrail;
import practice.Payouts.Claim;
import practice.Payouts.PayoutPolicy;
import practice.Payouts.Transfers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayoutsTest {

    /** A bank that remembers each transfer and numbers its references. */
    static final class FakeBank implements Transfers {
        final List<String> sent = new ArrayList<>();

        public String send(String account, long cents) {
            sent.add(account + " " + cents);
            return "TX-" + (sent.size() + 400);
        }
    }

    private final FakeBank bank = new FakeBank();
    private final List<String> trail = new ArrayList<>();
    private final AuditTrail audit = trail::add;

    @Test
    void paysApprovedClaimsAndRecordsEachPayment() {
        PayoutPolicy policy = new PayoutPolicy(bank, audit);

        List<String> references = policy.pay(List.of(
                new Claim("CL-88", "DE44-5001", 74_900, true),
                new Claim("CL-91", "FR76-3000", 1_250, true)));

        assertThat(references).containsExactly("TX-401", "TX-402");
        assertThat(bank.sent).containsExactly("DE44-5001 74900", "FR76-3000 1250");
        assertThat(trail).containsExactly("paid CL-88 TX-401", "paid CL-91 TX-402");
    }

    @Test
    void aDeclinedClaimIsRecordedInTheTrailItWasGiven() {
        PayoutPolicy policy = new PayoutPolicy(bank, audit);

        List<String> references = policy.pay(List.of(
                new Claim("CL-12", "ES91-2100", 30_000, false),
                new Claim("CL-13", "IT60-0542", 8_800, true),
                new Claim("CL-14", "BE68-5390", 0, true)));

        assertThat(references).containsExactly("TX-401");
        assertThat(bank.sent).containsExactly("IT60-0542 8800");
        assertThat(trail).containsExactly("skipped CL-12", "paid CL-13 TX-401", "skipped CL-14");
    }

    @Test
    void aPolicyWithoutTransfersIsRefusedWhenItIsBuilt() {
        assertThatThrownBy(() -> new PayoutPolicy(null, audit)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void aPolicyWithoutAnAuditTrailIsRefusedWhenItIsBuilt() {
        assertThatThrownBy(() -> new PayoutPolicy(bank, null)).isInstanceOf(NullPointerException.class);
    }
}
