package practice;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The search service's index: for each word, the ids of the documents that contain it. Indexing
 * threads add and remove documents while search threads look words up and walk the results.
 */
public class WordIndex {

    private final ConcurrentHashMap<String, Set<String>> documents;

    public WordIndex() {
        this(new ConcurrentHashMap<>());
    }

    /** Keeps the index in the given map, which the admin endpoints share. */
    public WordIndex(ConcurrentHashMap<String, Set<String>> documents) {
        this.documents = documents;
    }

    /** The documents that contain the word. Search threads walk this set while indexing goes on. */
    public Set<String> documentsWith(String word) {
        return documents.getOrDefault(word, Set.of());
    }

    /** Whether any document contains the word. */
    public boolean knows(String word) {
        return documents.containsKey(word);
    }

    /** Records that the document contains the word. */
    public void add(String word, String documentId) {
        Set<String> ids = documents.get(word);
        if (ids == null) {
            ids = ConcurrentHashMap.newKeySet();
            documents.put(word, ids);
        }
        ids.add(documentId);
    }

    /** Records that the document no longer contains the word; a word no document contains is dropped. */
    public void remove(String word, String documentId) {
        documents.computeIfPresent(word, (w, ids) -> {
            ids.remove(documentId);
            return ids.isEmpty() ? null : ids;
        });
    }
}
