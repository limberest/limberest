package io.limberest.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.util.BooleanFormatException;

public class JsonMatcher implements Predicate<Entry<String,String>> {
    
    private JSONObject json;
    
    public JsonMatcher(JSONObject json) {
        this.json = json;
    }
    
    /**
     * Match query filters and search.
     */
    @Override
    public boolean test(Entry<String,String> e) {
        
        if (e.getKey().equals("search")) {
            if (search(e.getValue()))
                return true;
        }
        
        if (!json.has(e.getKey()))
            return "null".equals(e.getValue());
        Object value = json.get(e.getKey());
        String stringVal = value.toString();
        if (isNumber(value)) {
            return matchNumber(e.getValue(), Double.valueOf(stringVal));
        }
        else if (value instanceof Boolean) {
            return matchBoolean(e.getValue(), (Boolean)value);
        }
        else if (value instanceof JSONObject) {
            // TODO syntax for compound JSONObjects
            return new JsonMatcher((JSONObject)value).test(e);
        }
        else if (value instanceof JSONArray) {
            // TODO syntax for JSONArrays
            return false;
        }
        else {
            return matchString(e.getValue(), stringVal);
        }
        
    }
    
    public boolean search(String find) {
        for (String jsonName : JSONObject.getNames(json)) {
            Object value = json.get(jsonName);
            if (searchMatch(find, value))
                return true;
        }
        return false;
    }
    
    protected boolean searchMatch(String find, Object value) {
        String stringVal = value.toString();
        if (value instanceof BigDecimal || value instanceof Integer) {
            try {
                Double.parseDouble(find);
                if (matchNumber(find, Double.valueOf(stringVal)))
                    return true;
            }
            catch (NumberFormatException ex) {
            }
        }
        else if (value instanceof JSONObject) {
            if (new JsonMatcher((JSONObject)value).search(find))
                return true;
        }
        else if (value instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray)value;
            for (int i = 0; i < jsonArr.length(); i++) {
                Object arrVal = jsonArr.get(i);
                if (searchMatch(find, arrVal))
                    return true;
            }
        }
        else {
            if (searchString(find, stringVal))
                return true;
        }
        return false;
    }
    
    /**
     * Override for case-sensitive or exact match.
     */
    protected boolean matchString(String filterVal, String value) {
        return value != null && value.toLowerCase().contains(filterVal.toLowerCase());
    }

    protected boolean searchString(String find, String value) {
        return value != null && value.toLowerCase().contains(find.toLowerCase());
    }

    protected boolean matchNumber(String filterVal, double value) {
        if (filterVal.startsWith("<=")) {
            return filterVal.length() > 2 && Double.parseDouble(filterVal.substring(2)) >= value;
        }
        else if (filterVal.startsWith("<")) {
            return filterVal.length() > 1 && Double.parseDouble(filterVal.substring(1)) > value;
        }
        else if (filterVal.startsWith(">=")) {
            return filterVal.length() > 2 && Double.parseDouble(filterVal.substring(2)) <= value;
        }
        else if (filterVal.startsWith(">")) {
            return filterVal.length() > 1 && Double.parseDouble(filterVal.substring(1)) < value;
        }
        else {
            return Double.parseDouble(filterVal) == value;
        }
    }
    
    protected boolean matchBoolean(String filterVal, boolean value) {
        return value == parseBoolean(filterVal);
    }
    
    static boolean parseBoolean(String s) {
        if (s == null)
            return false;
        if (s.equalsIgnoreCase("true"))
            return true;
        else if (s.equalsIgnoreCase("false") || s.equals("null"))
            return false;
        else
            throw BooleanFormatException.forInputString(s);
    }
    
    public static boolean isNumber(Object value) {
        return value instanceof BigDecimal || value instanceof BigInteger
                || value instanceof Long || value instanceof Double || value instanceof Integer;
    }
}
