package practice;

import java.util.concurrent.ConcurrentHashMap;

public final class WordCounter {

    public WordCounter(ConcurrentHashMap<String, Integer> counts) {
    }

    /** Adds one to the word's count, atomically. */
    public void add(String word) {
        throw new UnsupportedOperationException("write add");
    }

    /** Returns the word's count, 0 when it was never added. */
    public int count(String word) {
        throw new UnsupportedOperationException("write count");
    }
}
