package practice;

import java.util.List;

public final class NewsAgency {

    @FunctionalInterface
    public interface Observer<T> {
        void update(String event, T data);
    }

    public void addObserver(Observer<String> observer) {
        throw new UnsupportedOperationException("TODO");
    }

    public void removeObserver(Observer<String> observer) {
        throw new UnsupportedOperationException("TODO");
    }

    public void publishArticle(String article) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<String> getPublishedArticles() {
        throw new UnsupportedOperationException("TODO");
    }

    public int observerCount() {
        throw new UnsupportedOperationException("TODO");
    }
}
