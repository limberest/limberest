package io.limberest.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Default binding for Jsonables.
 * TODO: more thorough bean model caching
 */
public class Jsonator {

    private Jsonable jsonable;

    public Jsonator(Jsonable jsonable) {
        this.jsonable = jsonable;
    }

    public JSONObject getJson() {
        return getJson(new JsonObject());
    };

    /**
     * TODO: edge cases with swagger dataType attribute (eg: date)
     */
    public JSONObject getJson(JSONObject json) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(jsonable.getClass());
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                Method reader = pd.getReadMethod();
                if (reader != null && !isHidden(reader)) {
                  Object o = reader.invoke(jsonable, (Object[])null);
                  if (o instanceof Jsonable) {
                      Jsonable j = (Jsonable) o;
                      String jsonName = j.jsonName();
                      if (jsonName == null)
                          jsonName = pd.getName();
                      json.put(jsonName, j.toJson());
                  }
                  else {
                      o = getJsonObject(o);
                      if (o != null) {
                          if (o instanceof Boolean) {
                              if ((Boolean)o || (json instanceof JsonObject
                                      && JsonObject.getFormat() != null && JsonObject.getFormat().falseValuesOutput))
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
        JsonObject json = new JsonObject();
        for (final Entry<?,?> entry : map.entrySet()) {
            Object val = entry.getValue();
            String name = String.valueOf(entry.getKey());
            if (val instanceof Jsonable) {
                Jsonable j = (Jsonable) val;
                String jsonName = j.jsonName();
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

    private static Map<Method,Boolean> hiddenMethods;
    /**
     * For swagger "hidden" annotation, lowest declaring subclass governs.
     * Uses reflection to avoid dependency on the swagger stack.
     * This method is especially expensive, so preliminary caching begins here.
     */
    protected boolean isHidden(Method method) {
        if (hiddenMethods == null)
            hiddenMethods = new HashMap<>();
        Boolean hidden = hiddenMethods.get(method);
        if (hidden != null)
            return hidden;

        hidden = false;
        if ("getClass".equals(method.getName()) && method.getParameterTypes().length == 0)
            hidden = true;
        if ("getJson".equals(method.getName()) && method.getParameterTypes().length == 0)
            hidden = true;
        if ("getJsonName".equals(method.getName()) && method.getParameterTypes().length == 0)
            hidden = true;

        Class<?> c = method.getDeclaringClass();
        while (c != null) {
            try {
                Method m = c.getMethod(method.getName(), method.getParameterTypes());
                Annotation[] as = m.getAnnotations();
                if (as != null) {
                    for (Annotation a : as) {
                    	if (a.getClass().getName().equals("io.swagger.annotations.ApiModelProperty")) {
                    		Method hiddenMethod = a.getClass().getMethod("hidden");
                    		hidden = hiddenMethod.invoke(a).equals(Boolean.TRUE);
                    	}
                    }

                }
            }
            catch (ReflectiveOperationException ex) {
            }
            c = c.getSuperclass();
        }

        hiddenMethods.put(method, hidden);
        return hidden;
    }

    protected Object getJsonObject(Object o) {
        if (o == null) {
            return null;
        }
        else if (o instanceof JSONObject) {
            return o;
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
        else if (o instanceof Date) {
            return ((Date)o).toInstant().toString();
        }
        else if (o instanceof LocalDate) {
            return ((LocalDate)o).toString();
        }
        else if (o instanceof Instant) {
            return ((Instant)o).toString();
        }
        else if (o.getClass().isEnum()) {
            return o.toString();
        }
        else {
            return JSONObject.wrap(o);
        }
    }
}
