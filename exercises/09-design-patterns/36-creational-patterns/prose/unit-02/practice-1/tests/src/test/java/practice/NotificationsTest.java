package practice;

import org.junit.jupiter.api.Test;

import practice.Notifications.EmailFactory;
import practice.Notifications.Notification;
import practice.Notifications.NotificationFactory;
import practice.Notifications.SmsFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationsTest {

    @Test
    void emailFactorySendsAnEmail() {
        assertThat(new EmailFactory().notify("hi")).isEqualTo("Email: hi");
        assertThat(new EmailFactory().notify("order 1042 shipped")).isEqualTo("Email: order 1042 shipped");
    }

    @Test
    void smsFactorySendsAnSms() {
        assertThat(new SmsFactory().notify("hi")).isEqualTo("SMS: hi");
    }

    @Test
    void aNewCreatorNeedsNoChangeToTheBase() {
        NotificationFactory push = new NotificationFactory() {
            @Override
            protected Notification createNotification() {
                return message -> "Push: " + message;
            }
        };

        assertThat(push.notify("hi")).isEqualTo("Push: hi");
    }

    @Test
    void aNullProductIsReported() {
        NotificationFactory broken = new NotificationFactory() {
            @Override
            protected Notification createNotification() {
                return null;
            }
        };

        assertThatThrownBy(() -> broken.notify("hi")).isInstanceOf(IllegalStateException.class);
    }
}
