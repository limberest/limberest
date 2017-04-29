package io.limberest.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.swagger.annotations.ApiModelProperty;

/**
 * Default binding for Jsonables.
 * TODO: bean model caching
 */
public class Jsonator {
    
    private Jsonable jsonable;
    
    public Jsonator(Jsonable jsonable) {
        this.jsonable = jsonable;
    }
    
    /**
     * TODO: test with no swagger
     * TODO: edge cases with swagger dataType attribute (eg: date)
     */
    public JSONObject getJson() {
        JSONObject json = new JsonObject();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(jsonable.getClass());
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                Method reader = pd.getReadMethod();
                if (reader != null && !isHidden(reader)) {
                  Object o = reader.invoke(jsonable, (Object[])null);
                  if (o instanceof Jsonable) {
                      Jsonable j = (Jsonable) o;
                      String jsonName = j.getJsonName();
                      if (jsonName == null)
                          jsonName = pd.getName();
                      json.put(jsonName, j.toJson());
                  }
                  else {
                      o = getJsonObject(o);
                      if (o != null) {
                          if (o instanceof Boolean) {
                              // TODO configurable whether to serialize false
                              if ((Boolean)o)
                                  json.put(pd.getName(), o);
                          }
                          else {
                              json.put(pd.getName(), o);
                          }
                      }
                  }
                }
            }
            
            return json;
            
        }
        catch (IntrospectionException ex) {
            throw new JSONException(ex);
        }
        catch (ReflectiveOperationException ex) {
            throw new JSONException(ex);
        }
    }
    
    protected JSONArray getJson(Collection<?> coll) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Object o : coll) {
            o = getJsonObject(o);
            if (o != null)
                jsonArray.put(o);
        }
        return jsonArray;
    }
    
    protected JSONObject getJson(Map<?,?> map) throws JSONException {
        JSONObject json = new JsonObject();
        for (final Entry<?,?> entry : map.entrySet()) {
            Object val = entry.getValue();
            String name = String.valueOf(entry.getKey());
            if (val instanceof Jsonable) {
                Jsonable j = (Jsonable) val;
                String jsonName = j.getJsonName();
                if (jsonName == null)
                    jsonName = name;
                json.put(jsonName, j.toJson());
            }
            else {
                val = getJsonObject(val);
                if (val != null)
                    json.put(name, val);
            }
        }
        return json;
    }
    
    /**
     * For swagger "hidden" annotation, lowest declaring subclass governs.
     */
    protected boolean isHidden(Method method) {
        
        if ("getClass".equals(method.getName()) && method.getParameterTypes().length == 0)
            return true;
        if ("getJson".equals(method.getName()) && method.getParameterTypes().length == 0)
            return true;
        if ("getJsonName".equals(method.getName()) && method.getParameterTypes().length == 0)
            return true;
        
        Class<?> c = method.getDeclaringClass();
        while (c != null) {
            try {
                Method m = c.getMethod(method.getName(), method.getParameterTypes());
                ApiModelProperty apiModelProp = m.getAnnotation(ApiModelProperty.class);
                if (apiModelProp != null)
                    return apiModelProp.hidden();
            }
            catch (NoSuchMethodException ex) {
            }
            c = c.getSuperclass();
        }
        
        return false;
    }
    
    protected Object getJsonObject(Object o) {
        if (o == null) {
            return null;
        }
        else if (o instanceof JSONObject) {
            return (JSONObject)o;
        }
        else if (o instanceof Jsonable) {
            return ((Jsonable)o).toJson();
        }
        else if (o instanceof Collection) {
            return getJson((Collection<?>)o);
        }
        else if (o instanceof Map) {
            return getJson((Map<?,?>)o);
        }
        else {
            return JSONObject.wrap(o);
        }
    }    
}
