package io.limberest.json;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;

/**
 * Extends org.json.JSONObject to offer predictable ordering of object keys.
 * This is especially useful for comparing expected versus actual stringified JSON results
 * (for either automated or eyeball comparisons).
 */
public class JsonObject extends JSONObject {

    public JsonObject() {
    }

    public JsonObject(String source) {
        super(source);
    }

    public Set<String> keySet() {
        return new TreeSet<String>(super.keySet());
    }

    protected Set<Entry<String,Object>> entrySet() {
        Set<Entry<String,Object>> entries = new TreeSet<>(new Comparator<Entry<String,Object>>() {
            public int compare(Entry<String,Object> e1, Entry<String,Object> e2) {
                return e1.getKey().compareTo(e2.getKey());
            }
        });
        entries.addAll(super.entrySet());
        return entries;
    }
}
