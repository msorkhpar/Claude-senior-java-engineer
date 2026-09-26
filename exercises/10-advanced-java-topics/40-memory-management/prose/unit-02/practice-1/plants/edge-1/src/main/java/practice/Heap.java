package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class Heap {

    private static final class Obj {
        final String name;
        final List<Obj> refs = new ArrayList<>();
        boolean marked;

        Obj(String name) {
            this.name = name;
        }
    }

    private final Map<String, Obj> objects = new LinkedHashMap<>();
    private final Set<Obj> roots = new LinkedHashSet<>();
    private final List<String> allocationOrder = new ArrayList<>();

    private Obj get(String name) {
        Obj o = objects.get(name);
        if (o == null) {
            throw new NoSuchElementException("no object " + name);
        }
        return o;
    }

    /** Adds an object; a name already in use is refused. */
    public void allocate(String name) {
        if (objects.containsKey(name)) {
            throw new IllegalArgumentException("already allocated: " + name);
        }
        objects.put(name, new Obj(name));
        allocationOrder.add(name);
    }

    /** Makes object 'from' refer to object 'to'. */
    public void reference(String from, String to) {
        get(from).refs.add(get(to));
    }

    public void addRoot(String name) {
        roots.add(get(name));
    }

    public void removeRoot(String name) {
        roots.remove(get(name));
    }

    /** Marks from the roots, sweeps, and returns the freed names in allocation order. */
    public List<String> collect() {
        List<String> freed = new ArrayList<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            var it = objects.values().iterator();
            while (it.hasNext()) {
                Obj o = it.next();
                if (roots.contains(o)) {
                    continue;
                }
                int count = 0;
                for (Obj other : objects.values()) {
                    for (Obj r : other.refs) {
                        if (r == o) {
                            count++;
                        }
                    }
                }
                if (count == 0) {
                    freed.add(o.name);
                    it.remove();
                    changed = true;
                }
            }
        }
        List<String> inOrder = new ArrayList<>();
        for (String name : allocationOrder) {
            if (freed.contains(name)) {
                inOrder.add(name);
            }
        }
        return inOrder;
    }

    private void mark(Obj start) {
        List<Obj> pending = new ArrayList<>();
        pending.add(start);
        while (!pending.isEmpty()) {
            Obj o = pending.remove(pending.size() - 1);
            if (o.marked) {
                continue;
            }
            o.marked = true;
            pending.addAll(o.refs);
        }
    }

    /** Names of the objects still allocated, in allocation order. */
    public List<String> live() {
        return new ArrayList<>(objects.keySet());
    }
}
