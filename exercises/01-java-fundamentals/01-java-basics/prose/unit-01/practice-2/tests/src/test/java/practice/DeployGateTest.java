package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.DeployGate.Verdict.HOLD;
import static practice.DeployGate.Verdict.PROMOTE;

class DeployGateTest {

    @Test
    void promotesAHealthyCanaryAndHoldsAFailingOne() {
        DeployGate gate = new DeployGate(0.02);

        assertThat(gate.judge(4_990, 5_000)).isEqualTo(PROMOTE);
        assertThat(gate.judge(4_000, 5_000)).isEqualTo(HOLD);
        assertThat(gate.judge(0, 80)).isEqualTo(HOLD);
    }

    @Test
    void aCanaryExactlyAtTheLimitIsPromoted() {
        DeployGate gate = new DeployGate(0.3);

        assertThat(gate.judge(7, 10)).isEqualTo(PROMOTE);       // 3 failures in 10: exactly 30 %
    }

    @Test
    void aCanaryThatServedNoRequestsIsHeld() {
        DeployGate gate = new DeployGate(0.3);

        assertThat(gate.judge(0, 0)).isEqualTo(HOLD);
    }
}
