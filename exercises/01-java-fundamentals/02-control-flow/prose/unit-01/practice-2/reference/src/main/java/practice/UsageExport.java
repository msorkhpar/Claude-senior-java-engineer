package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes one line per tenant for the nightly usage export, which the billing system reads.
 */
public class UsageExport {

    /** One measured value: a count of events, or a ratio between 0 and 1. */
    public record Metric(String name, boolean isRatio, int count, double ratio) {
        public static Metric count(String name, int count) {
            return new Metric(name, false, count, 0);
        }

        public static Metric ratio(String name, double ratio) {
            return new Metric(name, true, 0, ratio);
        }
    }

    private final Set<String> freeTenants;
    private final Map<String, Integer> contractQuotas;

    public UsageExport(Set<String> freeTenants, Map<String, Integer> contractQuotas) {
        this.freeTenants = freeTenants;
        this.contractQuotas = contractQuotas;
    }

    /** The export line for one tenant, for example {@code globex;quota=250;jobs=12;failure-rate=0.5}. */
    public String line(String tenant, List<Metric> metrics) {
        List<String> fields = new ArrayList<>();
        fields.add(tenant);
        Integer quota = quota(tenant);
        fields.add("quota=" + (quota == null ? "unlimited" : quota.toString()));
        for (Metric metric : metrics) {
            fields.add(metric.name() + "=" + value(metric));
        }
        return String.join(";", fields);
    }

    /**
     * The tenant's job quota: 100 on the free plan, otherwise the quota in its contract, or null
     * when the contract sets none (unlimited).
     */
    Integer quota(String tenant) {
        if (freeTenants.contains(tenant)) {
            return 100;
        } else {
            return contractQuotas.get(tenant);
        }
    }

    /** The value written for a metric: an Integer for a count, a Double for a ratio. */
    Object value(Metric metric) {
        if (metric.isRatio()) {
            return metric.ratio();
        } else {
            return metric.count();
        }
    }
}
