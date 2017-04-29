package io.limberest.json;

import java.util.Comparator;

import org.json.JSONObject;

public class JsonComparator implements Comparator<JSONObject> {

    private String sort;
    private int direction = 1;
    private Comparator<JSONObject> fallback;

    public JsonComparator(String sort) {
        this(sort, false, null);
    }
    
    public JsonComparator(String sort, boolean descending, Comparator<JSONObject> fallback) {
        this.sort = sort;
        if (descending)
            direction = -1;
    }
    
    /**
     * Comparator to use by default, and as secondary sort when
     * primary sort values are identical.
     */
    private Comparator<JSONObject> getFallback() {
        return fallback;
    };
    
    @Override
    public int compare(JSONObject j1, JSONObject j2) {
        // no sort specified
        if (sort == null && direction == 1) {
            Comparator<JSONObject> fallback = getFallback();
            if (fallback == null)
                return 0;
            else
                return fallback.compare(j1, j2);
        }
        
        int i = compareValues(getSortValue(sort, j1), getSortValue(sort, j2));
        if (i == 0) {
            Comparator<JSONObject> fallback = getFallback();
            if (fallback != null)
                i = fallback.compare(j1, j2);
        }
        return i;
    }
    
    /**
     * Override to customize sort value for key. 
     */
    protected Object getSortValue(String name, JSONObject json) {
        if (json.has(name))
            return json.get(name);
        else
            return null;
    }
    
    /**
     * Override to change default behavior.
     * TODO: compound objects and arrays
     */
    protected int compareValues(Object o1, Object o2) {
        if (o1 == null) {
            return direction * (o2 == null ? 0 : -1);
        }
        else {
            if (o2 == null)
                return direction;
            
            if (JsonMatcher.isNumber(o1)) {
                return direction * (Double.valueOf(o1.toString()).compareTo(Double.valueOf(o2.toString())));
            }
            else if (o1 instanceof String) {
                return direction * ((String)o1).compareToIgnoreCase(o2.toString());
            }
            else {
                return 0;
            }
        }
    }
}
