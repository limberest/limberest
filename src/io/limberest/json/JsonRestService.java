package io.limberest.json;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.json.JSONException;
import org.json.JSONObject;

import io.limberest.service.ServiceException;
import io.limberest.service.http.RestService;
import io.limberest.service.http.Status;

@Consumes("application/json")
@Produces("application/json")
public abstract class JsonRestService extends RestService<JSONObject> {

    @Override
    public JSONObject getBody(String text) throws ServiceException {
        try {
            return new JsonObject(text);
        }
        catch (JSONException ex) {
            throw new ServiceException(Status.BAD_REQUEST, ex.getMessage());
        }
    }

    @Override
    public String getText(JSONObject json, int prettyIndent) {
        return json.toString(prettyIndent);
    }
}
