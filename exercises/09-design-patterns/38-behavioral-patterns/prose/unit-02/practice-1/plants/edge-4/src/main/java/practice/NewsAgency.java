package practice;

import java.util.ArrayList;
import java.util.List;

public final class NewsAgency {

    @FunctionalInterface
    public interface Observer<T> {
        void update(String event, T data);
    }

    private final List<Observer<String>> observers = new ArrayList<>();
    private final List<String> publishedArticles = new ArrayList<>();

    public void addObserver(Observer<String> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(Observer<String> observer) {
        if (!observers.remove(observer)) {
            throw new IllegalArgumentException("Observer is not registered");
        }
    }

    public void publishArticle(String article) {
        if (article == null || article.isBlank()) {
            throw new IllegalArgumentException("Article cannot be null or blank");
        }
        publishedArticles.add(article);
        for (Observer<String> observer : new ArrayList<>(observers)) {
            observer.update("NEW_ARTICLE", article);
        }
    }

    public List<String> getPublishedArticles() {
        return List.copyOf(publishedArticles);
    }

    public int observerCount() {
        return observers.size();
    }
}
