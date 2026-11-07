package practice;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRequestTest {

    @Test
    void aClientsQueryBecomesARequest() {
        PageRequest req = PageRequest.fromQuery(" 6", "40 ");
        assertThat(req.page()).isEqualTo(6);
        assertThat(req.size()).isEqualTo(40);

        PageRequest noSize = PageRequest.fromQuery("11", null);
        assertThat(noSize.page()).isEqualTo(11);
        assertThat(noSize.size()).isEqualTo(PageRequest.DEFAULT_SIZE);

        assertThat(PageRequest.fromQuery("0", "1").next()).isEqualTo(new PageRequest(1, 1));
    }

    @Test
    void aQueryForANegativePageOrNoRowsIsRefused() {
        assertThatThrownBy(() -> PageRequest.fromQuery("-2", "40"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageRequest.fromQuery("3", "0"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageRequest.fromQuery("3", "-15"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void everyWayOfMakingARequestRefusesAnImpossibleOne() {
        assertThatThrownBy(() -> new PageRequest(-4))
                .as("a saved search with a negative page").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PageRequest(7, 0))
                .as("a request with size 0").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PageRequest(Integer.MAX_VALUE, 30).next())
                .as("the page after the last int").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anOversizedRequestIsTheSameRequestAsOneAtTheLimit() {
        PageRequest huge = PageRequest.fromQuery("2", "5000");
        PageRequest atLimit = new PageRequest(2, PageRequest.MAX_SIZE);

        assertThat(huge.size()).isEqualTo(PageRequest.MAX_SIZE);
        assertThat(huge).isEqualTo(atLimit);
        assertThat(huge.hashCode()).isEqualTo(atLimit.hashCode());
        Set<PageRequest> cachedPages = new HashSet<>(List.of(atLimit));
        assertThat(cachedPages.contains(new PageRequest(2, 999))).isTrue();
    }
}
