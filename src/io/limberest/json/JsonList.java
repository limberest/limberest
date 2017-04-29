package io.limberest.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Binds a java.util.List of Jsonables to either a JSONArray or a
 * JSONObject.
 */
public class JsonList<E extends Jsonable> implements Jsonable {

    private boolean isObject;

    private List<E> list;
    public List<E> getList() { return list; }

    private String jsonName;
    public String getJsonName() { return jsonName; }

    /**
     * Useful when list is paginated.
     */
    private int total = -1;
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public JsonList(List<E> list, String jsonName) {
        this(list, jsonName, false);
    }
    
    public JsonList(JSONObject json, Class<E> type) {
        this(json, type, false);
    }
    
    /**
     * @param json serialized list json
     * @param type java class for Jsonable elements
     * @param isObject true means jsonify as object with properties for items,
     *          false means jsonify as array
     */
    public JsonList(JSONObject json, Class<E> type, boolean isObject) {
        this.isObject = isObject;
        list = new ArrayList<>();
        String[] names = JSONObject.getNames(json);
        if (names.length != 1)
            throw new JSONException("Expected a single top-level object");
        jsonName = names[0];
        if (isObject) {
            JSONObject jsonObj = json.getJSONObject(jsonName);
            for (String name : JSONObject.getNames(jsonObj)) {
                JSONObject jsonItem = jsonObj.getJSONObject(name);
                E jsonable = createJsonable(jsonItem, type, name);
                list.add(jsonable);
            }
        }
        else {
            JSONArray jsonArr = json.getJSONArray(jsonName);
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonItem = jsonArr.getJSONObject(i);
                E jsonable = createJsonable(jsonItem, type);
                list.add(jsonable);
            }
        }
    }

    /**
     * @param list list of Jsonables
     * @param jsonName name for the JSONObject
     * @param isObject true means jsonify as object with properties for items,
     *          false means jsonify as array
     */
    public JsonList(List<E> list, String jsonName, boolean isObject) {
        this.list = list;
        this.jsonName = jsonName;
        this.isObject = isObject;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JsonObject();
        if (isObject) {
            JSONObject jsonObj = new JsonObject();
            for (Jsonable item : list) {
                String jsonName = item.getJsonName();
                if (jsonName == null) {
                    // default to the Class name in lower camel
                    String ucName = item.getClass().getSimpleName();
                    jsonName = ucName.substring(0, 1).toLowerCase() + ucName.substring(1);
                }
                jsonObj.put(item.getJsonName(), item.toJson());
            }
            json.put(jsonName, jsonObj);
        }
        else {
            JSONArray jsonArr = new JSONArray();
            for (Jsonable item : list) {
                jsonArr.put(item.toJson());
            }
            json.put(jsonName, jsonArr);
        }
        if (total >= 0)
            json.put("total", total);
        return json;
    }
    
    private E createJsonable(JSONObject json, Class<E> type) {
        try {
            Constructor<E> ctor = type.getConstructor(JSONObject.class);
            return ctor.newInstance(json);
        }
        catch (Exception ex) {
            throw new JSONException("Unable to create Jsonable type: " + type, ex);
        }
    }
    
    private E createJsonable(JSONObject json, Class<E> type, String name) {
        try {
            try {
                Constructor<E> ctor = type.getConstructor(JSONObject.class, String.class);
                return ctor.newInstance(json, name);
            }
            catch (NoSuchMethodException ex) {
                Constructor<E> ctor = type.getConstructor(JSONObject.class);
                E inst = ctor.newInstance(json);
                try {
                    Method meth = type.getMethod("setName", String.class);
                    meth.invoke(inst, name);
                }
                catch (NoSuchMethodException ignore) {
                    // no name to set
                }
                return inst;
            }
        }
        catch (Exception ex) {
            throw new JSONException("Unable to create Jsonable type: " + type, ex);
        }
    }
}
