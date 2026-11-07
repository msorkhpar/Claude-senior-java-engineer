package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Downloads the pages of a paginated report in parallel: one task per page, run on a pool, and
 * the results put back in page order.
 */
public class PageFetcher {

    /** Fetches one page of the report; pages are numbered from 1. */
    public interface Client {
        String fetch(int page);
    }

    private final Client client;

    public PageFetcher(Client client) {
        this.client = client;
    }

    public List<String> fetchAll(int pageCount, ExecutorService pool) throws Exception {
        List<String> pages = new ArrayList<>();
        for (Future<String> page : pool.invokeAll(tasks(pageCount))) {
            pages.add(page.get());
        }
        return pages;
    }

    /** One task per page, 1 to pageCount, in page order. */
    List<Callable<String>> tasks(int pageCount) {
        // TODO: Build the tasks the pool runs. The pool may run them in any order and at any time
        //       after this method returns; each must still fetch its own page through client.
        throw new IllegalStateException("TODO: tasks");
    }
}
