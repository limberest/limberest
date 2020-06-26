package io.limberest.json;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;

import io.limberest.config.LimberestConfig;

/**
 * Extends org.json.JSONObject to offer formatting options,
 * especially predictable ordering of object keys.
 */
public class JsonObject extends JSONObject {

    private static LimberestConfig.JsonFormat format = LimberestConfig.getSettings().json();
    public static LimberestConfig.JsonFormat getFormat() { return format; }

    public JsonObject() {
    }

    public JsonObject(String source) {
        super(source);
    }

    @Override
    public Set<String> keySet() {
        if (format == null || format.orderedKeys)
            return new TreeSet<String>(super.keySet());
        else
            return super.keySet();
    }

    @Override
    protected Set<Entry<String,Object>> entrySet() {
        if (format == null || format.orderedKeys) {
            Set<Entry<String,Object>> entries = new TreeSet<>(new Comparator<Entry<String,Object>>() {
                public int compare(Entry<String,Object> e1, Entry<String,Object> e2) {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
            entries.addAll(super.entrySet());
            return entries;
        }
        else {
            return super.entrySet();
        }
    }
}
