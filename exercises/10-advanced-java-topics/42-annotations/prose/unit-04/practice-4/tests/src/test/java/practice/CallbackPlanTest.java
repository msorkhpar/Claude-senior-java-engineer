package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallbackPlanTest {

    static class Cache {
        final List<String> log = new ArrayList<>();

        @CallbackPlan.OnStart(order = 2)
        void warmUp() {
            log.add("warmUp");
        }

        @CallbackPlan.OnStart(order = 3)
        private void announce() {
            log.add("announce");
        }

        @CallbackPlan.OnStart(order = 1)
        public void open() {
            log.add("open");
        }

        @CallbackPlan.OnStart(order = 6)
        void report() {
            log.add("report");
        }

        @CallbackPlan.OnStart(order = 0)
        void allocate() {
            log.add("allocate");
        }

        @CallbackPlan.OnStart(order = 5)
        void index() {
            log.add("index");
        }

        @CallbackPlan.OnStart(order = 4)
        void load() {
            log.add("load");
        }

        public void close() {
            log.add("close");
        }
    }

    static class Broken {
        @CallbackPlan.OnStart
        void open(String url) {
        }

        @CallbackPlan.OnStart
        void ready() {
        }

        @CallbackPlan.OnStart
        void load(int n) {
        }
    }

    @Test
    void plansAndRunsTheCallbacksInOrder() throws Exception {
        CallbackPlan plan = CallbackPlan.of(Cache.class);
        Cache first = new Cache();
        Cache second = new Cache();

        plan.run(first);
        plan.run(second);

        assertThat(plan.names()).containsExactly("allocate", "open", "warmUp", "announce", "load", "index", "report");
        assertThat(first.log).containsExactly("allocate", "open", "warmUp", "announce", "load", "index", "report");
        assertThat(second.log).containsExactly("allocate", "open", "warmUp", "announce", "load", "index", "report");
    }

    @Test
    void aBrokenCallbackFailsWhenThePlanIsBuilt() {
        assertThatThrownBy(() -> CallbackPlan.of(Broken.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theMessageNamesEveryBrokenCallback() {
        assertThatThrownBy(() -> CallbackPlan.of(Broken.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("open")
                .hasMessageContaining("load")
                .hasMessageNotContaining("ready");
    }
}
