package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Heap {

    /** One object on the heap: a name, and the references it holds to other objects. */
    public static final class Obj {
        private final String name;
        private final List<Obj> refs = new ArrayList<>();

        public Obj(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public List<Obj> refs() {
            return refs;
        }

        /** Makes this object refer to each of {@code others}, and returns this object. */
        public Obj refersTo(Obj... others) {
            refs.addAll(List.of(others));
            return this;
        }
    }

    private Heap() {
    }

    /** Returns the names of the objects in {@code heap} that a collector must keep alive. */
    public static Set<String> live(List<Obj> heap, List<Obj> roots) {
        throw new UnsupportedOperationException("write live");
    }
}
