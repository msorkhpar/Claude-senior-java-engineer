package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A heap dump as a leak hunter's tool reads it: every object in the heap with the objects its
 * fields refer to, and the GC roots (static fields, the local variables of live threads).
 */
public class HeapDump {

    /** One object of the dump; ids are unique within a dump. */
    public static final class Obj {
        private final String id;
        private final List<Obj> refs = new ArrayList<>();

        public Obj(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        /** The objects this object's fields refer to. */
        public List<Obj> refs() {
            return refs;
        }

        /** Records that this object's fields refer to {@code others}; returns this object. */
        public Obj refersTo(Obj... others) {
            Collections.addAll(refs, others);
            return this;
        }
    }

    /** The ids of the objects that a collection would free, sorted, for the tool's report. */
    public static Set<String> garbage(List<Obj> heap, List<Obj> roots) {
        Set<Obj> kept = retained(heap, roots);
        Set<String> ids = new TreeSet<>();
        for (Obj o : heap) {
            if (!kept.contains(o)) ids.add(o.id());
        }
        return ids;
    }

    /** The objects of {@code heap} that a collection must keep. */
    static Set<Obj> retained(List<Obj> heap, List<Obj> roots) {
        Set<Obj> kept = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Obj> toVisit = new ArrayDeque<>(roots);
        while (!toVisit.isEmpty()) {
            Obj o = toVisit.pop();
            if (kept.add(o)) toVisit.addAll(o.refs());
        }
        return kept;
    }
}
