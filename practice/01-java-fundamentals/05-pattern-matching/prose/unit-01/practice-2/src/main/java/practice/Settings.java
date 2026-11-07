package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Settings decoded at startup into plain Java values: a YAML sequence arrives as a List, a value
 * from the command line as a String[], and one from an older plugin API as an Object[].
 */
public class Settings {

    private final Map<String, Object> values;

    public Settings(Map<String, Object> values) {
        this.values = values;
    }

    /** The setting as a list of strings; a setting that isn't there is an empty list. */
    public List<String> strings(String key) {
        Object value = values.get(key);
        if (value == null) {
            return List.of();
        }
        return stringList(value);
    }

    /** The value's strings, in order. */
    static List<String> stringList(Object value) {
        // TODO: Accept a list or an array whose items are all strings. For anything else, throw
        //       IllegalArgumentException here, at startup, so a wrong setting never reaches
        //       the code that reads the list later.
        throw new IllegalStateException("TODO: stringList");
    }
}
