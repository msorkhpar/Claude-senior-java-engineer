package practice;

import org.junit.jupiter.api.Test;
import practice.Refunds.Err;
import practice.Refunds.Ok;
import practice.Refunds.Order;
import practice.Refunds.Refund;
import practice.Refunds.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RefundsTest {

    /** A gateway that records every call and answers with the given result. */
    static final class RecordingGateway implements Refunds.Gateway {
        final List<String> calls = new ArrayList<>();
        final Result<String> answer;

        RecordingGateway(Result<String> answer) { this.answer = answer; }

        @Override
        public Result<String> send(String orderId, long cents) {
            calls.add(orderId + ":" + cents);
            return answer;
        }
    }

    static Refunds.Orders store(Order... orders) {
        Map<String, Order> byId = new java.util.HashMap<>();
        for (Order o : orders) byId.put(o.id(), o);
        return id -> byId.containsKey(id)
                ? new Ok<>(byId.get(id))
                : new Err<>("no order with id " + id);
    }

    static final Order BOOTS = new Order("R-5512", 8_990, 1_000);

    @Test
    void refundsAnAllowedAmountThroughTheGateway() {
        RecordingGateway gateway = new RecordingGateway(new Ok<>("GW-3301"));
        Result<Refund> r = new Refunds(store(BOOTS), gateway).refund("R-5512", 4_000);

        assertThat(r).isEqualTo(new Ok<>(new Refund("R-5512", 4_000, "GW-3301")));
        assertThat(gateway.calls).containsExactly("R-5512:4000");
    }

    @Test
    void anUnknownOrderIsAFailureAndNothingIsSent() {
        RecordingGateway gateway = new RecordingGateway(new Ok<>("GW-3302"));
        Result<Refund> r = new Refunds(store(BOOTS), gateway).refund("R-0404", 500);

        assertThat(r).isInstanceOf(Err.class);
        assertThat(gateway.calls).isEmpty();
    }

    @Test
    void aRefusedAmountIsNeverSent() {
        RecordingGateway gateway = new RecordingGateway(new Ok<>("GW-3303"));
        Result<Refund> r = new Refunds(store(BOOTS), gateway).refund("R-5512", 0);

        assertThat(r).isInstanceOf(Err.class);
        assertThat(gateway.calls).isEmpty();
    }

    @Test
    void anAmountAboveWhatIsLeftComesBackAsAFailure() {
        RecordingGateway gateway = new RecordingGateway(new Ok<>("GW-3304"));
        Result<Refund> r = new Refunds(store(BOOTS), gateway).refund("R-5512", 7_991);

        assertThat(r).isEqualTo(new Err<>("only 7990 cents left to refund on R-5512"));
        assertThat(gateway.calls).isEmpty();
    }

    @Test
    void aFailureKeepsTheReasonOfTheStepThatRefused() {
        RecordingGateway gateway = new RecordingGateway(new Ok<>("GW-3305"));
        Result<Refund> unknown = new Refunds(store(BOOTS), gateway).refund("R-0405", 500);

        RecordingGateway refusing = new RecordingGateway(new Err<>("issuer closed the account"));
        Result<Refund> bank = new Refunds(store(BOOTS), refusing).refund("R-5512", 700);

        assertThat(unknown).isEqualTo(new Err<>("no order with id R-0405"));
        assertThat(bank).isEqualTo(new Err<>("issuer closed the account"));
    }
}
