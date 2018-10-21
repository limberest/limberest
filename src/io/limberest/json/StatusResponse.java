package io.limberest.json;

import org.json.JSONObject;

import io.limberest.service.http.Status;
import io.swagger.annotations.ApiModelProperty;

public class StatusResponse implements Jsonable {

    @ApiModelProperty(required=true)
    private Status status;
    public Status getStatus() { return status; }

    public StatusResponse(Status status) {
        this.status = status;
    }

    public StatusResponse(Status status, String message) {
        this.status = new Status(status, message);
    }

    public StatusResponse(int code, String message) {
        this.status = new Status(code, message);
    }

    @Override
    public JSONObject toJson() {
        JsonObject json = new JsonObject();
        JsonObject statusJson = new JsonObject();
        json.put("status", statusJson);
        statusJson.put("code", status.getCode());
        statusJson.put("message", status.getMessage());
        return json;
    }

    public String toString() {
        return toJson().toString(2);
    }
}
