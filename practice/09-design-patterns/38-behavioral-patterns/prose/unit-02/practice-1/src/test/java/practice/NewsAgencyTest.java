package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import practice.NewsAgency.Observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NewsAgencyTest {

    /** A subscriber that records what it receives; two with the same name are equal. */
    static final class Subscriber implements Observer<String> {
        final String name;
        final List<String> received = new ArrayList<>();

        Subscriber(String name) {
            this.name = name;
        }

        @Override
        public void update(String event, String data) {
            received.add(event + ":" + data);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Subscriber other && other.name.equals(name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    @Test
    void everySubscriberGetsEachArticle() {
        NewsAgency agency = new NewsAgency();
        Subscriber alice = new Subscriber("alice");
        Subscriber bob = new Subscriber("bob");
        agency.addObserver(alice);
        agency.addObserver(bob);

        agency.publishArticle("Java 21");
        agency.removeObserver(bob);
        agency.publishArticle("Records");

        assertThat(alice.received).containsExactly("NEW_ARTICLE:Java 21", "NEW_ARTICLE:Records");
        assertThat(bob.received).containsExactly("NEW_ARTICLE:Java 21");
        assertThat(agency.getPublishedArticles()).containsExactly("Java 21", "Records");
        assertThat(agency.observerCount()).isEqualTo(1);
        assertThatThrownBy(() -> agency.addObserver(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aSecondRegistrationIsIgnored() {
        NewsAgency agency = new NewsAgency();
        Subscriber alice = new Subscriber("alice");
        agency.addObserver(alice);
        agency.addObserver(alice);
        agency.addObserver(new Subscriber(new String("alice")));

        agency.publishArticle("x");

        assertThat(alice.received).containsExactly("NEW_ARTICLE:x");
        assertThat(agency.observerCount()).isEqualTo(1);
    }

    @Test
    void aBlankArticleIsRefused() {
        NewsAgency agency = new NewsAgency();
        Subscriber alice = new Subscriber("alice");
        agency.addObserver(alice);

        assertThatThrownBy(() -> agency.publishArticle("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> agency.publishArticle("\t")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> agency.publishArticle(null)).isInstanceOf(IllegalArgumentException.class);

        assertThat(alice.received).isEmpty();
        assertThat(agency.getPublishedArticles()).isEmpty();
    }

    @Test
    void callersCannotChangeThePublishedList() {
        NewsAgency agency = new NewsAgency();
        agency.publishArticle("first");
        List<String> seen = agency.getPublishedArticles();

        try {
            seen.add("forged");
        } catch (UnsupportedOperationException refused) {
            // an unmodifiable answer is fine too
        }
        agency.publishArticle("second");

        assertThat(agency.getPublishedArticles()).containsExactly("first", "second");
    }

    @Test
    void removingAStrangerIsHarmless() {
        NewsAgency agency = new NewsAgency();
        Subscriber alice = new Subscriber("alice");
        agency.addObserver(alice);

        agency.removeObserver(new Subscriber("carol"));
        agency.removeObserver(null);
        agency.publishArticle("still here");

        assertThat(alice.received).containsExactly("NEW_ARTICLE:still here");
        assertThat(agency.observerCount()).isEqualTo(1);
    }
}
