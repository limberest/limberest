package io.limberest.json;

import org.json.JSONObject;

import io.swagger.annotations.ApiModelProperty;

/**
 * By convention Jsonables should have a constructor that takes
 * a single argument of type org.json.JSONObject.
 */
public interface Jsonable {

    /**
     * Builds a JSON object representing this.
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
    
    default void bind(JSONObject json) {
        new Objectifier(this).from(json);
    }
}
