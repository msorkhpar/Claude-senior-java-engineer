package practice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class GenerationalHeap {

    public enum Generation { EDEN, SURVIVOR, OLD }

    private static final class Obj {
        Generation generation = Generation.EDEN;
        int age;
    }

    private final int tenuringThreshold;
    private final Map<String, Obj> objects = new LinkedHashMap<>();

    public GenerationalHeap(int tenuringThreshold) {
        if (tenuringThreshold < 0) {
            throw new IllegalArgumentException("threshold must not be negative: " + tenuringThreshold);
        }
        this.tenuringThreshold = tenuringThreshold;
    }

    /** A new object in Eden, age 0. */
    public void allocate(String id) {
        if (objects.containsKey(id)) {
            throw new IllegalArgumentException("already allocated: " + id);
        }
        objects.put(id, new Obj());
    }

    /** Collects the young generation; returns the freed ids in allocation order. */
    public List<String> minorGc(Set<String> reachable) {
        List<String> freed = new ArrayList<>();
        Iterator<Map.Entry<String, Obj>> it = objects.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Obj> e = it.next();
            Obj o = e.getValue();
            if (o.generation == Generation.OLD) {
                continue;
            }
            if (!reachable.contains(e.getKey())) {
                freed.add(e.getKey());
                it.remove();
                continue;
            }
            o.age++;
            o.generation = o.age > tenuringThreshold ? Generation.OLD : Generation.SURVIVOR;
        }
        return freed;
    }

    /** Collects every generation; returns the freed ids in allocation order. */
    public List<String> majorGc(Set<String> reachable) {
        List<String> freed = new ArrayList<>();
        Iterator<String> it = objects.keySet().iterator();
        while (it.hasNext()) {
            String id = it.next();
            if (!reachable.contains(id)) {
                freed.add(id);
                it.remove();
            }
        }
        return freed;
    }

    private Obj get(String id) {
        Obj o = objects.get(id);
        if (o == null) {
            throw new NoSuchElementException("no object " + id);
        }
        return o;
    }

    public Generation generation(String id) {
        return get(id).generation;
    }

    public int age(String id) {
        return get(id).age;
    }
}
