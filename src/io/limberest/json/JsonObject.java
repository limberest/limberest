package io.limberest.json;

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
}
