package io.limberest.json;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.json.JSONObject;

import io.limberest.service.http.RestService;

@Consumes("application/json")
@Produces("application/json")
public abstract class JsonRestService extends RestService<JSONObject> {

    @Override
    public JSONObject getBody(String text) {
        return new JsonObject(text);
    }

    @Override
    public String getText(JSONObject json, int prettyIndent) {
        return json.toString(prettyIndent);
    }
}
