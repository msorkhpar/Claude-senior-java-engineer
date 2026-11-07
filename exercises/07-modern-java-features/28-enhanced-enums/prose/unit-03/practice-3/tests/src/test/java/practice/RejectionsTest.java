package practice;

import org.junit.jupiter.api.Test;

import java.util.Map;

import practice.Rejections.Reason;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RejectionsTest {

    /** A carrier's delivery exceptions, a table of the same shape as the reasons. */
    record DeliveryProblem(String code, String meaning) {}

    @Test
    void findsTheReasonForEveryCodeTheBankSends() {
        assertThat(Reason.fromCode("AM04")).contains(Reason.INSUFFICIENT_FUNDS);
        assertThat(Reason.fromCode("AC06")).contains(Reason.BLOCKED_ACCOUNT);
        for (Reason reason : Reason.values()) {
            assertThat(Reason.fromCode(new String(reason.code()))).contains(reason);
        }
    }

    @Test
    void indexesEachItemUnderItsCode() {
        DeliveryProblem[] table = {
                new DeliveryProblem("D07", "recipient not home"),
                new DeliveryProblem("D12", "address unreadable"),
                new DeliveryProblem("R03", "refused at the door")};

        Map<String, DeliveryProblem> byCode = Rejections.index(table, DeliveryProblem::code);

        assertThat(byCode).hasSize(3);
        assertThat(byCode.get("D12")).isSameAs(table[1]);
        assertThat(byCode.get("R03")).isSameAs(table[2]);
    }

    @Test
    void refusesATableWithACodeUsedTwice() {
        DeliveryProblem[] table = {
                new DeliveryProblem("D07", "recipient not home"),
                new DeliveryProblem("D12", "address unreadable"),
                new DeliveryProblem("D07", "parcel damaged")};

        assertThatThrownBy(() -> Rejections.index(table, DeliveryProblem::code))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("D07");
    }

    @Test
    void aCodeWeDoNotKnowYetIsReportedAsUnknown() {
        assertThat(Reason.fromCode("AG11")).isEmpty();
        assertThat(Reason.fromCode("am04")).isEmpty();
    }
}
