package practice;

import org.junit.jupiter.api.Test;
import practice.AuditLog.Amount;
import practice.AuditLog.Channel;
import practice.AuditLog.Discount;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogTest {

    @Test
    void namesThePlainConstantsAndTheRecords() {
        assertThat(AuditLog.typeName(Channel.PHONE)).isEqualTo("Channel");
        assertThat(AuditLog.typeName(Discount.NONE)).isEqualTo("Discount");
        assertThat(AuditLog.typeName(new Amount(4_250))).isEqualTo("Amount");
    }

    @Test
    void tellsTypesApart() {
        assertThat(AuditLog.sameType(Channel.WEB, Channel.PARTNER)).isTrue();
        assertThat(AuditLog.sameType(new Amount(990), new Amount(12_000))).isTrue();
        assertThat(AuditLog.sameType(Channel.WEB, Discount.NONE)).isFalse();
        assertThat(AuditLog.sameType(new Amount(990), Channel.PHONE)).isFalse();
    }

    @Test
    void writesAnEditBetweenTwoPlainConstants() {
        assertThat(AuditLog.line("channel", Channel.WEB, Channel.PARTNER))
                .isEqualTo("channel: Channel WEB -> PARTNER");
        assertThat(AuditLog.line("total", new Amount(990), Channel.PHONE))
                .isEqualTo("total: type changed from Amount to Channel");
    }

    @Test
    void namesTheEnumOfAConstantThatHasABody() {
        assertThat(AuditLog.typeName(Discount.LOYALTY)).isEqualTo("Discount");
        assertThat(AuditLog.typeName(Discount.VOUCHER)).isEqualTo("Discount");
    }

    @Test
    void aConstantWithABodyIsTheSameTypeAsTheOtherConstants() {
        assertThat(AuditLog.sameType(Discount.NONE, Discount.VOUCHER)).isTrue();
        assertThat(AuditLog.sameType(Discount.LOYALTY, Discount.VOUCHER)).isTrue();
        assertThat(AuditLog.sameType(Discount.LOYALTY, Channel.WEB)).isFalse();
    }
}
