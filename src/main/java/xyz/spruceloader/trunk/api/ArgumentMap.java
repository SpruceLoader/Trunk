package xyz.spruceloader.trunk.api;

import java.util.*;

// TODO use Optionals
// null is evil >:(
public class ArgumentMap {
    private final Map<String, List<String>> internalMap = new HashMap<>();
    private boolean updated = false; // We will use this to cache things later on
    private String[] asArray; // We cache this map as an array, so it's slightly faster

    public boolean has(String key) {
        return internalMap.containsKey(key);
    }

    public String getSingular(String key) {
        return internalMap.get(key).get(0);
    }

    public List<String> getAll(String key) {
        return internalMap.get(key);
    }

    public void putIfAbsent(String key, String value) {
        boolean has = has(key);
        if (!has)
            updated = true;
        internalMap.putIfAbsent(key, new ArrayList<>(Collections.singletonList(value)));
    }

    public void put(String key, String value) {
        updated = true;
        List<String> values = getAll(key);
        if (values == null)
            values = new ArrayList<>();

        values.add(value);
        internalMap.put(key, values);
    }

    public void remove(String key) {
        updated = true;
        internalMap.remove(key);
    }

    public String[] toArray() {
        if (!updated && asArray != null)
            return asArray;
        List<String> returnValue = new ArrayList<>();
        internalMap.forEach((key, value) -> value.forEach(item -> {
            returnValue.add("--" + key);
            returnValue.add(item);
        }));
        String[] asArray = returnValue.toArray(new String[0]);
        this.asArray = asArray;
        updated = false;
        return asArray;
    }

    public static ArgumentMap parse(String[] rawInput) {
        ArgumentMap returnValue = new ArgumentMap();
        List<String> input = Arrays.asList(rawInput);
        for (String arg : input) {
            int index = input.indexOf(arg);
            if (input.size() - 1 < index || !arg.startsWith("--"))
                continue;

            String value = index + 1 >= input.size() ? "" : input.get(index + 1);
            if (value.startsWith("--"))
                value = ""; // This is an empty argument

            String name = arg.substring(2); // Remove the arg name identifier
            returnValue.put(name, value);
        }
        return returnValue;
    }
}
