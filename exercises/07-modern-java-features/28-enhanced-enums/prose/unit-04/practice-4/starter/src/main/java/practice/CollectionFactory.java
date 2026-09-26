package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

public enum CollectionFactory {
    ARRAY_LIST {

        @Override
        public <T> Collection<T> create() {
            throw new UnsupportedOperationException("write this method");
        }

        @Override
        public <T> Collection<T> createFrom(Collection<T> source) {
            throw new UnsupportedOperationException("write this method");
        }
    },
    HASH_SET {
        @Override
        public <T> Collection<T> create() {
            throw new UnsupportedOperationException("write this method");
        }

        @Override
        public <T> Collection<T> createFrom(Collection<T> source) {
            throw new UnsupportedOperationException("write this method");
        }
    },
    TREE_SET {
        @Override
        public <T> Collection<T> create() {
            throw new UnsupportedOperationException("write this method");
        }

        @Override
        public <T> Collection<T> createFrom(Collection<T> source) {
            throw new UnsupportedOperationException("write this method");
        }
    };

    /** A new, empty collection of this constant's kind. */
    public abstract <T> Collection<T> create();

    /** A new collection of this constant's kind holding a copy of {@code source}'s elements. */
    public abstract <T> Collection<T> createFrom(Collection<T> source);

    /** A collection of this constant's kind holding {@code elements}. */
    @SafeVarargs
    public final <T> Collection<T> of(T... elements) {
        throw new UnsupportedOperationException("write of");
    }
}
