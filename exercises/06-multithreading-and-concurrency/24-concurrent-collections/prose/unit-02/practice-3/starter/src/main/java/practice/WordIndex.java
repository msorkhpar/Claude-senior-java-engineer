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
        // TODO: Record the document under the word. Documents for one word, new or not, arrive
        //       from many indexing threads at once, while search threads walk the word's set.
        throw new IllegalStateException("TODO: add");
    }

    /** Records that the document no longer contains the word; a word no document contains is dropped. */
    public void remove(String word, String documentId) {
        // TODO: Take the document off the word, and drop the word once no document has it. A
        //       document for the same word may be added at that very moment, and must be kept.
        throw new IllegalStateException("TODO: remove");
    }
}
