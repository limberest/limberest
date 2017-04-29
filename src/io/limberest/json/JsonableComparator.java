package io.limberest.json;

import java.util.Comparator;

import org.json.JSONObject;

import io.limberest.service.Query;

public class JsonableComparator implements Comparator<Jsonable> {

    private JsonComparator jsonComparator;
    public JsonableComparator(Query query, Comparator<JSONObject> fallback) {
        jsonComparator = new JsonComparator(query.getSort(), query.isDescending(), fallback);
    }

    @Override
    public int compare(Jsonable j1, Jsonable j2) {
        return jsonComparator.compare(j1.toJson(), j2.toJson());
    }
}
