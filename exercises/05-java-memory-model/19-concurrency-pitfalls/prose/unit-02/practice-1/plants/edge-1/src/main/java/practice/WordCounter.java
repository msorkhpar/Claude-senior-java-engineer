package practice;

import java.util.concurrent.ConcurrentHashMap;

public final class WordCounter {

    private final ConcurrentHashMap<String, Integer> counts;

    public WordCounter(ConcurrentHashMap<String, Integer> counts) {
        this.counts = counts;
    }

    /** Adds one to the word's count, atomically. */
    public void add(String word) {
        counts.put(word, counts.getOrDefault(word, 0) + 1);
    }

    /** Returns the word's count, 0 when it was never added. */
    public int count(String word) {
        return counts.getOrDefault(word, 0);
    }
}
