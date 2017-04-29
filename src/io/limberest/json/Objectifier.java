package io.limberest.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Binds a JSONObject to a JavaBean.
 * TODO: bean info caching
 *
 */
public class Objectifier {
    
    private Object into;
    
    public Objectifier(Object into) {
        this.into = into;
    }
    
    public void from(JSONObject json) throws JSONException {
        for (String name : JSONObject.getNames(json)) {
            try {
                Method writer = getWriter(name);
                if (writer != null) {
                    Object o = json.get(name);
                    Type t = writer.getGenericParameterTypes()[0];
                    o = getObject(t, o);
                    if (o != null)
                        writer.invoke(into, new Object[]{o});
                }
            }
            catch (IllegalArgumentException ex) {
                throw new JSONException(ex.getMessage() + ": " + name, ex);
            }
            catch (IntrospectionException ex) {
                throw new JSONException(ex.getMessage() + ": " + name, ex);
            }
            catch (ReflectiveOperationException ex) {
                throw new JSONException(ex.getMessage() + ": " + name, ex);
            }
        }
    }

    protected Method getWriter(String name) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(into.getClass());
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (name.equals(pd.getName()))
                return pd.getWriteMethod();
        }
        return null;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object getObject(Type t, Object o) throws ReflectiveOperationException {
        Type[] ats = null;
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            t = pt.getRawType();
            ats = pt.getActualTypeArguments();
        }

        if (o instanceof JSONObject) {
            JSONObject json = (JSONObject)o;
            if (t instanceof Class) {
                if (Jsonable.class.isAssignableFrom((Class<?>)t)) {
                    Class<? extends Jsonable> jclass = (Class<? extends Jsonable>)t;
                    try {
                        Constructor<? extends Jsonable> jctor = jclass.getConstructor(JSONObject.class);
                        return jctor.newInstance(json);
                    }
                    catch (NoSuchMethodException ex) {
                        throw new JSONException(t + " must implement a constructor that takes a JSONObject");
                    }
                }
                if (Map.class.isAssignableFrom((Class<?>)t)) {
                    Class<?> clazz = (Class<?>)t;
                    Map map = getMapInstance((Class<? extends Map>)clazz);
                    for (String name : JSONObject.getNames(json)) {
                        Object jo = json.get(name);
                        if (ats != null && ats.length == 2 && ats[0].equals(String.class)) {
                            Object mo = getObject(ats[1], jo);
                            map.put(name, mo);
                        }
                    }
                }
            }
        }
        else if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray)o;
            if (t instanceof Class) {
                Class<?> clazz = (Class<?>)t;
                if (Collection.class.isAssignableFrom(clazz)) {
                    Collection coll = getCollectionInstance((Class<? extends Collection>)clazz);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Object ao = jsonArray.get(i);
                        Object co = getObject(ats == null || ats.length == 0 ? ao.getClass() : ats[0], ao);
                        coll.add(co);
                    }
                    return coll;
                }
            }
        }
        else if (o instanceof Number) {
            if (t.equals(o.getClass()))
                return o;
            else
                return coerceNumber((Number)o, t);
        }
        else if (o instanceof Boolean || o instanceof String) {
            return o;
        }
        
        return null;
    }
    
    
    /**
     * Tries to force a number to match an expected type.
     * @param n number to coerce
     * @param t type
     * @return object of expected type
     */
    protected Object coerceNumber(Number n, Type t) throws ReflectiveOperationException {
        Class<? extends Number> c = primitiveNumToWrapper.get(t.getTypeName());
        if (n.getClass().equals(c))
            return n; // runtime coercion will work
        Constructor<? extends Number> ctor = c.getConstructor(String.class);
        return ctor.newInstance(String.valueOf(n));
    }
    
    private static final Map<String,Class<? extends Number>> primitiveNumToWrapper = new HashMap<>();
    static {
        primitiveNumToWrapper.put("double", Double.class);
        primitiveNumToWrapper.put("float", Float.class);
        primitiveNumToWrapper.put("int", Integer.class);
        primitiveNumToWrapper.put("long", Long.class);
        primitiveNumToWrapper.put("short", short.class);        
    }
    
    /**
     * Creates a collection instance to which bound JSONArray element objects will be added.
     * If these default impls are not appropriate, this behavior can be overridden in a Jsonable constructor.
     * @param collectionType the raw collection class or interface
     * @return empty instance of the default collection implementation
     */
    @SuppressWarnings("rawtypes")
    protected Collection getCollectionInstance(Class<? extends Collection> collectionType) throws ReflectiveOperationException {
        if (collectionType.isInterface()) {
            if (List.class.isAssignableFrom(collectionType))
                return new ArrayList<>();
            else if (Set.class.isAssignableFrom(collectionType))
                return new HashSet<>();
            else if (Multiset.class.isAssignableFrom(collectionType))
                return HashMultiset.create();
            else
                throw new JSONException("Unsupported collection type: " + collectionType);
        }
        else {
            return collectionType.newInstance();
        }
    }
    
    /**
     * Creates a map instance to which bound JSONObject elements will be added.
     * If these default impls are not appropriate, this behavior can be overridden in a Jsonable constructor.
     * @param mapType the raw collection class or interface
     * @return empty instance of the default map implementation
     */
    @SuppressWarnings("rawtypes")
    protected Map getMapInstance(Class<? extends Map> mapType) throws ReflectiveOperationException {
        if (mapType.isInterface()) {
            if (HashMap.class.isAssignableFrom(mapType))
                return new LinkedHashMap<>();
            else if (Dictionary.class.isAssignableFrom(mapType))
                return new Hashtable<>();
            else if (SortedMap.class.isAssignableFrom(mapType))
                return new TreeMap<>();
            else
                throw new JSONException("Unsupported map type: " + mapType);
        }
        else {
            return mapType.newInstance();
        }
    }
}
