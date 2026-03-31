package com.github.msorkhpar.claudejavatutor.behavioralpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Observer Pattern Tests")
class ObserverPatternTest {

    // ===================== NewsAgency (Classic Observer) Tests =====================

    @Nested
    @DisplayName("NewsAgency and NewsSubscriber")
    class NewsAgencyTest {

        @Test
        @DisplayName("Should notify subscriber when article is published")
        void testNotifyOnPublish() {
            var agency = new ObserverPattern.NewsAgency();
            var subscriber = new ObserverPattern.NewsSubscriber("Alice");
            agency.addObserver(subscriber);

            agency.publishArticle("Breaking news!");

            assertThat(subscriber.getReceivedArticles()).containsExactly("Breaking news!");
        }

        @Test
        @DisplayName("Should notify multiple subscribers")
        void testMultipleSubscribers() {
            var agency = new ObserverPattern.NewsAgency();
            var sub1 = new ObserverPattern.NewsSubscriber("Alice");
            var sub2 = new ObserverPattern.NewsSubscriber("Bob");
            agency.addObserver(sub1);
            agency.addObserver(sub2);

            agency.publishArticle("Article 1");

            assertThat(sub1.getReceivedArticles()).containsExactly("Article 1");
            assertThat(sub2.getReceivedArticles()).containsExactly("Article 1");
        }

        @Test
        @DisplayName("Should stop notifying after observer is removed")
        void testRemoveObserver() {
            var agency = new ObserverPattern.NewsAgency();
            var subscriber = new ObserverPattern.NewsSubscriber("Alice");
            agency.addObserver(subscriber);

            agency.publishArticle("Article 1");
            agency.removeObserver(subscriber);
            agency.publishArticle("Article 2");

            assertThat(subscriber.getReceivedArticles()).containsExactly("Article 1");
        }

        @Test
        @DisplayName("Should not add duplicate observer")
        void testNoDuplicateObservers() {
            var agency = new ObserverPattern.NewsAgency();
            var subscriber = new ObserverPattern.NewsSubscriber("Alice");

            agency.addObserver(subscriber);
            agency.addObserver(subscriber);

            assertThat(agency.observerCount()).isEqualTo(1);

            agency.publishArticle("Test");
            assertThat(subscriber.getReceivedArticles()).hasSize(1);
        }

        @Test
        @DisplayName("Should track published articles")
        void testPublishedArticles() {
            var agency = new ObserverPattern.NewsAgency();
            agency.publishArticle("Article 1");
            agency.publishArticle("Article 2");

            assertThat(agency.getPublishedArticles()).containsExactly("Article 1", "Article 2");
        }

        @Test
        @DisplayName("Published articles list should be unmodifiable")
        void testPublishedArticlesUnmodifiable() {
            var agency = new ObserverPattern.NewsAgency();
            agency.publishArticle("Article 1");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> agency.getPublishedArticles().add("hacked"));
        }

        @Test
        @DisplayName("Should throw on null observer")
        void testNullObserver() {
            var agency = new ObserverPattern.NewsAgency();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> agency.addObserver(null));
        }

        @Test
        @DisplayName("Should throw on null article")
        void testNullArticle() {
            var agency = new ObserverPattern.NewsAgency();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> agency.publishArticle(null));
        }

        @Test
        @DisplayName("Should throw on blank article")
        void testBlankArticle() {
            var agency = new ObserverPattern.NewsAgency();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> agency.publishArticle("   "));
        }

        @Test
        @DisplayName("Should handle removing non-existent observer gracefully")
        void testRemoveNonExistentObserver() {
            var agency = new ObserverPattern.NewsAgency();
            var subscriber = new ObserverPattern.NewsSubscriber("Ghost");

            assertThatNoException().isThrownBy(() -> agency.removeObserver(subscriber));
        }

        @Test
        @DisplayName("Should not fail when publishing with no observers")
        void testPublishWithNoObservers() {
            var agency = new ObserverPattern.NewsAgency();

            assertThatNoException().isThrownBy(() -> agency.publishArticle("Orphan article"));
            assertThat(agency.getPublishedArticles()).containsExactly("Orphan article");
        }

        @Test
        @DisplayName("Subscriber name should be set correctly")
        void testSubscriberName() {
            var subscriber = new ObserverPattern.NewsSubscriber("Alice");

            assertThat(subscriber.getName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should throw on null subscriber name")
        void testNullSubscriberName() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ObserverPattern.NewsSubscriber(null));
        }

        @Test
        @DisplayName("Should throw on blank subscriber name")
        void testBlankSubscriberName() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ObserverPattern.NewsSubscriber("  "));
        }

        @Test
        @DisplayName("Received articles list should be unmodifiable")
        void testReceivedArticlesUnmodifiable() {
            var subscriber = new ObserverPattern.NewsSubscriber("Alice");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> subscriber.getReceivedArticles().add("hacked"));
        }
    }

    // ===================== EventBus Tests =====================

    @Nested
    @DisplayName("EventBus")
    class EventBusTest {

        @Test
        @DisplayName("Should deliver events to subscribers")
        void testEventDelivery() {
            var bus = new ObserverPattern.EventBus();
            List<Object> received = new ArrayList<>();

            bus.subscribe("click", received::add);
            bus.publish("click", "button1");

            assertThat(received).containsExactly("button1");
        }

        @Test
        @DisplayName("Should support multiple event types")
        void testMultipleEventTypes() {
            var bus = new ObserverPattern.EventBus();
            List<Object> clicks = new ArrayList<>();
            List<Object> hovers = new ArrayList<>();

            bus.subscribe("click", clicks::add);
            bus.subscribe("hover", hovers::add);
            bus.publish("click", "btn");
            bus.publish("hover", "img");

            assertThat(clicks).containsExactly("btn");
            assertThat(hovers).containsExactly("img");
        }

        @Test
        @DisplayName("Should not deliver events to unsubscribed listeners")
        void testUnsubscribe() {
            var bus = new ObserverPattern.EventBus();
            List<Object> received = new ArrayList<>();
            Consumer<Object> listener = received::add;

            bus.subscribe("event", listener);
            bus.publish("event", "data1");
            bus.unsubscribe("event", listener);
            bus.publish("event", "data2");

            assertThat(received).containsExactly("data1");
        }

        @Test
        @DisplayName("Should handle publishing to event with no listeners")
        void testPublishNoListeners() {
            var bus = new ObserverPattern.EventBus();

            assertThatNoException().isThrownBy(() -> bus.publish("ghost", "data"));
        }

        @Test
        @DisplayName("Should return correct listener counts")
        void testListenerCount() {
            var bus = new ObserverPattern.EventBus();
            assertThat(bus.listenerCount("click")).isEqualTo(0);

            bus.subscribe("click", o -> {});
            assertThat(bus.listenerCount("click")).isEqualTo(1);

            bus.subscribe("click", o -> {});
            assertThat(bus.listenerCount("click")).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return registered event types")
        void testEventTypes() {
            var bus = new ObserverPattern.EventBus();
            bus.subscribe("click", o -> {});
            bus.subscribe("hover", o -> {});

            assertThat(bus.eventTypes()).containsExactlyInAnyOrder("click", "hover");
        }

        @Test
        @DisplayName("Should throw on null event type for subscribe")
        void testNullEventTypeSubscribe() {
            var bus = new ObserverPattern.EventBus();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> bus.subscribe(null, o -> {}));
        }

        @Test
        @DisplayName("Should throw on null listener")
        void testNullListener() {
            var bus = new ObserverPattern.EventBus();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> bus.subscribe("event", null));
        }

        @Test
        @DisplayName("Should throw on null event type for publish")
        void testNullEventTypePublish() {
            var bus = new ObserverPattern.EventBus();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> bus.publish(null, "data"));
        }
    }

    // ===================== ThreadSafeSubject Tests =====================

    @Nested
    @DisplayName("ThreadSafeSubject")
    class ThreadSafeSubjectTest {

        @Test
        @DisplayName("Should notify observers in a thread-safe manner")
        void testThreadSafeNotification() throws InterruptedException {
            var subject = new ObserverPattern.ThreadSafeSubject<String>();
            AtomicInteger callCount = new AtomicInteger(0);

            ObserverPattern.Observer<String> observer = (event, data) -> callCount.incrementAndGet();
            subject.addObserver(observer);

            int numThreads = 10;
            CountDownLatch latch = new CountDownLatch(numThreads);
            try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
                for (int i = 0; i < numThreads; i++) {
                    executor.submit(() -> {
                        subject.notifyObservers("event", "data");
                        latch.countDown();
                    });
                }
                latch.await(5, TimeUnit.SECONDS);
            }

            assertThat(callCount.get()).isEqualTo(numThreads);
        }

        @Test
        @DisplayName("Should not add duplicate observer")
        void testNoDuplicateObserver() {
            var subject = new ObserverPattern.ThreadSafeSubject<String>();
            ObserverPattern.Observer<String> observer = (e, d) -> {};

            subject.addObserver(observer);
            subject.addObserver(observer);

            assertThat(subject.observerCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw on null observer")
        void testNullObserver() {
            var subject = new ObserverPattern.ThreadSafeSubject<String>();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> subject.addObserver(null));
        }

        @Test
        @DisplayName("Should remove observer correctly")
        void testRemoveObserver() {
            var subject = new ObserverPattern.ThreadSafeSubject<String>();
            ObserverPattern.Observer<String> observer = (e, d) -> {};

            subject.addObserver(observer);
            assertThat(subject.observerCount()).isEqualTo(1);

            subject.removeObserver(observer);
            assertThat(subject.observerCount()).isEqualTo(0);
        }
    }

    // ===================== ObservableProperty Tests =====================

    @Nested
    @DisplayName("ObservableProperty")
    class ObservablePropertyTest {

        @Test
        @DisplayName("Should notify listener on value change")
        void testNotifyOnChange() {
            var property = new ObserverPattern.ObservableProperty<>("initial");
            List<String> oldValues = new ArrayList<>();
            List<String> newValues = new ArrayList<>();

            property.addListener((oldVal, newVal) -> {
                oldValues.add(oldVal);
                newValues.add(newVal);
            });

            property.setValue("updated");

            assertThat(oldValues).containsExactly("initial");
            assertThat(newValues).containsExactly("updated");
        }

        @Test
        @DisplayName("Should not notify when value is the same")
        void testNoNotifyOnSameValue() {
            var property = new ObserverPattern.ObservableProperty<>("initial");
            AtomicInteger callCount = new AtomicInteger(0);

            property.addListener((o, n) -> callCount.incrementAndGet());
            property.setValue("initial"); // same value

            assertThat(callCount.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle null initial value")
        void testNullInitialValue() {
            var property = new ObserverPattern.ObservableProperty<String>(null);
            List<String> newValues = new ArrayList<>();

            property.addListener((o, n) -> newValues.add(n));
            property.setValue("value");

            assertThat(newValues).containsExactly("value");
        }

        @Test
        @DisplayName("Should handle setting value to null")
        void testSetToNull() {
            var property = new ObserverPattern.ObservableProperty<>("initial");
            List<String> newValues = new ArrayList<>();

            property.addListener((o, n) -> newValues.add(n));
            property.setValue(null);

            assertThat(newValues).containsExactly((String) null);
            assertThat(property.getValue()).isNull();
        }

        @Test
        @DisplayName("Should notify multiple listeners")
        void testMultipleListeners() {
            var property = new ObserverPattern.ObservableProperty<>(0);
            AtomicInteger totalCalls = new AtomicInteger(0);

            property.addListener((o, n) -> totalCalls.incrementAndGet());
            property.addListener((o, n) -> totalCalls.incrementAndGet());

            property.setValue(1);

            assertThat(totalCalls.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should remove listener")
        void testRemoveListener() {
            var property = new ObserverPattern.ObservableProperty<>(0);
            AtomicInteger callCount = new AtomicInteger(0);
            ObserverPattern.PropertyChangeListener<Integer> listener = (o, n) -> callCount.incrementAndGet();

            property.addListener(listener);
            property.setValue(1);
            assertThat(callCount.get()).isEqualTo(1);

            property.removeListener(listener);
            property.setValue(2);
            assertThat(callCount.get()).isEqualTo(1); // no additional call
        }

        @Test
        @DisplayName("Should track listener count")
        void testListenerCount() {
            var property = new ObserverPattern.ObservableProperty<>(0);
            assertThat(property.listenerCount()).isEqualTo(0);

            ObserverPattern.PropertyChangeListener<Integer> l1 = (o, n) -> {};
            property.addListener(l1);
            assertThat(property.listenerCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw on null listener")
        void testNullListener() {
            var property = new ObserverPattern.ObservableProperty<>(0);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> property.addListener(null));
        }
    }

    // ===================== FilteredObserver Tests =====================

    @Nested
    @DisplayName("FilteredObserver")
    class FilteredObserverTest {

        @Test
        @DisplayName("Should only receive matching events")
        void testFilteredEvents() {
            List<String> received = new ArrayList<>();
            ObserverPattern.Observer<String> baseObserver = (event, data) -> received.add(data);
            var filtered = new ObserverPattern.FilteredObserver<>(baseObserver, event -> event.startsWith("URGENT"));

            filtered.update("URGENT_NEWS", "Important!");
            filtered.update("REGULAR_NEWS", "Meh");
            filtered.update("URGENT_UPDATE", "Alert!");

            assertThat(received).containsExactly("Important!", "Alert!");
            assertThat(filtered.getReceivedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should pass through all events when filter accepts everything")
        void testPassAllFilter() {
            List<String> received = new ArrayList<>();
            ObserverPattern.Observer<String> baseObserver = (event, data) -> received.add(data);
            var filtered = new ObserverPattern.FilteredObserver<>(baseObserver, event -> true);

            filtered.update("A", "1");
            filtered.update("B", "2");

            assertThat(received).containsExactly("1", "2");
        }

        @Test
        @DisplayName("Should block all events when filter rejects everything")
        void testBlockAllFilter() {
            List<String> received = new ArrayList<>();
            ObserverPattern.Observer<String> baseObserver = (event, data) -> received.add(data);
            var filtered = new ObserverPattern.FilteredObserver<>(baseObserver, event -> false);

            filtered.update("A", "1");

            assertThat(received).isEmpty();
            assertThat(filtered.getReceivedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw on null delegate")
        void testNullDelegate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ObserverPattern.FilteredObserver<>(null, e -> true));
        }

        @Test
        @DisplayName("Should throw on null filter")
        void testNullFilter() {
            ObserverPattern.Observer<String> baseObserver = (event, data) -> {};

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ObserverPattern.FilteredObserver<>(baseObserver, null));
        }
    }
}
