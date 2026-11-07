package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class PriceFeedTest {

    private final PriceFeed feed = new PriceFeed();

    @Test
    void aWidgetHearsOnlyItsOwnSymbol() {
        List<Double> acme = new ArrayList<>();
        List<Double> globex = new ArrayList<>();
        feed.subscribe("ACME", acme::add);
        feed.subscribe("GLOBEX", globex::add);

        feed.tick("ACME", 41.5);
        feed.tick("GLOBEX", 7.25);
        feed.tick("ACME", 41.75);

        assertThat(acme).containsExactly(41.5, 41.75);
        assertThat(globex).containsExactly(7.25);
        assertThat(feed.subscriptionCount()).isEqualTo(2);
    }

    @Test
    void aClosedWidgetHearsNothingMore() {
        List<Double> heard = new ArrayList<>();
        PriceFeed.Subscription s = feed.subscribe("INITECH", heard::add);
        feed.tick("INITECH", 12.0);

        s.close();
        feed.tick("INITECH", 12.5);

        assertThat(heard).containsExactly(12.0);
        assertThat(feed.subscriptionCount()).isZero();
    }

    @Test
    void closingOneSymbolOfAListenerKeepsItsOtherSymbols() {
        List<String> heard = new ArrayList<>();
        DoubleConsumer blotter = price -> heard.add("price " + price);
        PriceFeed.Subscription acme = feed.subscribe("ACME", blotter);
        feed.subscribe("UMBRELLA", blotter);

        acme.close();
        feed.tick("ACME", 40.0);
        feed.tick("UMBRELLA", 3.5);

        assertThat(heard).containsExactly("price 3.5");
        assertThat(feed.subscriptionCount()).isEqualTo(1);
    }

    @Test
    void closingOneWidgetKeepsTheOthersOnTheSameSymbol() {
        List<Double> chart = new ArrayList<>();
        List<Double> ticker = new ArrayList<>();
        PriceFeed.Subscription chartSub = feed.subscribe("GLOBEX", chart::add);
        feed.subscribe("GLOBEX", ticker::add);

        chartSub.close();
        feed.tick("GLOBEX", 8.0);

        assertThat(chart).isEmpty();
        assertThat(ticker).containsExactly(8.0);
    }

    @Test
    void aListenerSubscribedTwiceToOneSymbolKeepsTheSubscriptionNotClosed() {
        List<Double> heard = new ArrayList<>();
        DoubleConsumer alert = heard::add;
        PriceFeed.Subscription fromTheWatchlist = feed.subscribe("HOOLI", alert);
        feed.subscribe("HOOLI", alert);                  // opened again from the alerts panel

        fromTheWatchlist.close();
        fromTheWatchlist.close();
        feed.tick("HOOLI", 101.0);

        assertThat(heard).containsExactly(101.0);
        assertThat(feed.subscriptionCount()).isEqualTo(1);
    }
}
