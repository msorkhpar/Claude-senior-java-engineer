package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class NotifiersTest {

    private static Notifiers.Notifier email() {
        return new Notifiers.EmailNotifier("a@example.org");
    }

    @Test
    void eachLayerAddsAfterTheWrappedResult() {
        Notifiers.Notifier sms = new Notifiers.SmsDecorator(email(), "+1");
        Notifiers.Notifier slack = new Notifiers.SlackDecorator(sms, "ops");

        assertThat(sms.send("alert")).isEqualTo("Email to a@example.org: alert | SMS to +1: alert");
        assertThat(slack.send("alert"))
                .isEqualTo("Email to a@example.org: alert | SMS to +1: alert | Slack #ops: alert");
    }

    @Test
    void everyMethodDelegates() {
        Notifiers.Notifier chain = new Notifiers.SlackDecorator(new Notifiers.SmsDecorator(email(), "+1"), "ops");

        assertThat(chain.getDescription()).isEqualTo("Email(a@example.org) + SMS(+1) + Slack(#ops)");
    }

    @Test
    void decoratingTwiceIsCumulative() {
        Notifiers.Notifier twice = new Notifiers.SmsDecorator(new Notifiers.SmsDecorator(email(), "+1"), "+2");

        assertThat(twice.send("alert"))
                .isEqualTo("Email to a@example.org: alert | SMS to +1: alert | SMS to +2: alert");
    }

    @Test
    void aNullWrappeeIsRefusedAtOnce() {
        assertThatNullPointerException().isThrownBy(() -> new Notifiers.SmsDecorator(null, "+1"));
        assertThatNullPointerException().isThrownBy(() -> new Notifiers.SlackDecorator(null, "ops"));
    }
}
