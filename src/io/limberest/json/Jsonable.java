package io.limberest.json;

import org.json.JSONObject;

import io.swagger.annotations.ApiModelProperty;

/**
 * By convention Jsonables should have a constructor that takes
 * a single argument of type 
 * <a href="https://stleary.github.io/JSON-java/org/json/JSONObject.html">org.json.JSONObject</a>.
 * To invoke default autobinding, the constructor can call {@link #bind(org.json.JSONObject)}.
 */
public interface Jsonable {

    /**
     * Builds an 
     * <a href="https://stleary.github.io/JSON-java/org/json/JSONObject.html">org.json.JSONObject</a> 
     * representing this.
     * @return a JSON object
     */
    @ApiModelProperty(hidden=true)
    default JSONObject toJson() {
        return new Jsonator(this).getJson();
    };

    /**
     * May be overridden to name the JSON object returned from {@link #toJson()}.
     */
    @ApiModelProperty(hidden=true)
    default String getJsonName() {
        return null;
    }
    
    /**
     * Binds this Jsonable to an
     * <a href="https://stleary.github.io/JSON-java/org/json/JSONObject.html">org.json.JSONObject</a>.
     * Call this from the constructor to invoke autobinding.
     * @param json the JSONObject to bind to
     */
    default void bind(JSONObject json) {
        new Objectifier(this).from(json);
    }
}
