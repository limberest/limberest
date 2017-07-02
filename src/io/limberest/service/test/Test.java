package io.limberest.service.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.ws.rs.Path;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.json.JsonObject;
import io.limberest.service.ServiceException;
import io.limberest.service.http.Request;
import io.limberest.service.http.Response;
import io.limberest.service.http.Status;

@Path("/limberest/tests/{group}/{method}/{name}")
public class Test extends Tests {

    @Override
    public Response<JSONObject> get(Request<JSONObject> request) throws ServiceException {
        String[] segments = request.getPath().getSegments();
        if (segments.length < 5)
            throw new ServiceException(Status.BAD_REQUEST, "Bad path: " + request.getPath());
        
        String testName = segments[4];
        if (segments.length > 5) {
            // test name contains slash(es)
            for (int i = 5; i < segments.length; i++) {
                testName += "/" + segments[i];
            }
        }
        if (request.getQuery().hasFilters())
            testName += "?" + request.getQuery();
        Map<String,File> testFiles = getTestFiles();
        File file = testFiles.get(segments[2]);
        if (file == null)
            throw new ServiceException(Status.NOT_FOUND, "Test group not found: " + segments[2]);

        try {
            JSONObject item = null;
            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
            JSONObject fileJson = new JsonObject(content);
            if (fileJson.has("item")) {
                JSONArray itemArr = fileJson.getJSONArray("item");
                for (int i = 0; i < itemArr.length(); i++) {
                    JSONObject itemObj = itemArr.getJSONObject(i);
                    JSONObject reqObj = itemObj.optJSONObject("request");
                    if (reqObj != null && reqObj.optString("method").equals(segments[3])
                            && itemObj.optString("name").equals(testName)) {
                        item = itemObj;
                        break;
                    }
                }
            }
            
            if (item == null)
                throw new ServiceException(Status.NOT_FOUND, "Test item not found: " + segments[3] + "/" + segments[4]);
            
            return new Response<>(item);
        }
        catch (IOException ex) {
            throw new ServiceException(Status.INTERNAL_ERROR, ex);
        }
    }
    
}
