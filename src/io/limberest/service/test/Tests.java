package io.limberest.service.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.config.LimberestConfig;
import io.limberest.config.LimberestConfig.Settings;
import io.limberest.json.JsonObject;
import io.limberest.json.JsonRestService;
import io.limberest.service.ServiceException;
import io.limberest.service.http.Request;
import io.limberest.service.http.Response;
import io.limberest.service.http.Status;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/limberest/tests")
@Api("Limberest test cases")
public class Tests extends JsonRestService {

    @ApiOperation(value="Retrieve a flat list of tests",
            notes="Test assets are in postman collection", responseContainer="List")
    public Response<JSONObject> get(Request<JSONObject> request) throws ServiceException {
        Map<String,File> testFiles = getTestFiles();
        JSONObject json = new JsonObject();
        try {
            for (String path : testFiles.keySet()) {
                File file = testFiles.get(path);
                String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
                JSONObject fileJson = new JsonObject(content);
                if (fileJson.has("item")) {
                    JSONArray itemArr = fileJson.getJSONArray("item");
                    // lightweight items is array of strings
                    JSONArray items = new JSONArray();
                    for (int i = 0; i < itemArr.length(); i++) {
                        items.put(itemArr.getJSONObject(i).get("name"));
                    }
                    json.put(path, items);
                }
            }
            
            return new Response<>(json);
        }
        catch (IOException ex) {
            throw new ServiceException(Status.INTERNAL_ERROR, ex);
        }
        
    }
    
    /**
     * TODO: caching
     */
    private Map<String,File> testFiles = new HashMap<>();
    private File testsDir;
    
    private Map<String,File> getTestFiles() throws ServiceException {
        String testsLoc = null;
        Settings settings = LimberestConfig.getSettings();
        Map<?,?> test = settings.getMap("tests");
        if (test != null)
            testsLoc = settings.get("location", test);
        if (testsLoc == null)
            throw new ServiceException(Status.NOT_FOUND, "tests.location not defined");
        testsDir = new File(testsLoc);
        if (!testsDir.isDirectory())
            throw new ServiceException(Status.NOT_FOUND, "tests.location not found");
        List<String> exts = new ArrayList<>();
        for (Object ext : settings.getList("extensions", test)) {
            exts.add(ext.toString());
        }
        if (exts.isEmpty())
            throw new ServiceException(Status.NOT_FOUND, "tests.extensions not defined");
        for (String ext : exts)
            findTestFiles(testsDir, ext);
        return testFiles;
    }
    
    private void findTestFiles(File dir, String ext) {
        for (File child : dir.listFiles()) {
            if (child.isDirectory()) {
                findTestFiles(child, ext);
            }
            else if (child.getName().endsWith(ext)) {
                String path = child.getPath().substring(testsDir.getPath().length() + 1);
                path = path.substring(0, path.length() - ext.length());
                this.testFiles.put(path, child);
            }
        }
    }
    
}
