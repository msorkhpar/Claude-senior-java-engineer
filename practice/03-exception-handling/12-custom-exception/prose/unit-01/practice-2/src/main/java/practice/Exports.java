package practice;

/**
 * Nightly customer exports. Each export runs on a worker JVM; when one fails, the job framework
 * serializes the failure with Java serialization and sends it to the coordinator, which logs it
 * and schedules a retry for that customer.
 */
public class Exports {

    /** A customer as the CRM client library returns it. Not Serializable, and not ours to change. */
    public static final class Customer {
        private final String id;
        private final String region;
        private final Object crmSession;              // the live session the record was loaded through

        public Customer(String id, String region, Object crmSession) {
            this.id = id;
            this.region = region;
            this.crmSession = crmSession;
        }

        public String id() {
            return id;
        }

        public String region() {
            return region;
        }
    }

    /** An export that failed on a worker. */
    public static class ExportFailedException extends Exception {
        private final Customer customer;

        public ExportFailedException(Customer customer, Throwable cause) {
            // TODO: Build the failure of this customer's export. It travels from the worker to the
            //       coordinator, which may be running the previous release of this class.
            super((String) null);
            throw new IllegalStateException("TODO: ExportFailedException");
        }

        /** The customer whose export failed, for the coordinator's retry. */
        public String getCustomerId() {
            // TODO: The id of the customer whose export failed, on the worker and on the coordinator.
            throw new IllegalStateException("TODO: getCustomerId");
        }
    }
}
