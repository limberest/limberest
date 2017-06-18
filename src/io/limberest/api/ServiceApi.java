package io.limberest.api;

import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.limberest.config.LimberestConfig;
import io.limberest.config.LimberestConfig.Settings;
import io.limberest.jackson.JacksonPrettyPrinter;
import io.limberest.json.JsonObject;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

/**
 * TODO: Caching.
 */
public class ServiceApi {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceApi.class);

    public enum Format {
        json,
        yaml
    }
    
    public JSONObject getSwaggerJson(String path) throws ServiceApiException {
        Swagger swagger = getSwagger(path);
        try {
            // Re-parse as JsonObject to ensure ordering of definitions and paths.
            // TODO: make this optional (see limberest.yaml comments in limberest-demo)
            JsonObject swaggerJson = new JsonObject(Json.mapper().writeValueAsString(swagger));
            if (swaggerJson.has("definitions"))
                swaggerJson.put("definitions", new JsonObject(swaggerJson.getJSONObject("definitions").toString()));
            if (swaggerJson.has("paths"))
                swaggerJson.put("paths", new JsonObject(swaggerJson.getJSONObject("paths").toString()));
            return swaggerJson;
        }
        catch (JsonProcessingException ex) {
            throw new ServiceApiException(ex.getMessage(), ex);
        }
    }
    
    public String getSwaggerString(String path, Format format) throws ServiceApiException {
        int prettyIndent = 0;
        Settings settings = LimberestConfig.getSettings();
        Map<?,?> api = settings.getMap("api");
        if (api != null)
            prettyIndent = settings.getInt("prettyIndent", api);
        return getSwaggerString(path, format, prettyIndent);
    }
    
    public String getSwaggerString(String path, Format format, int prettyIndent) 
    throws ServiceApiException {
        try {
            Swagger swagger = getSwagger(path);
            // TODO orderedKeys in limberest.yaml
            boolean orderedKeys = true;
            if (format == Format.json && orderedKeys) {
                JSONObject swaggerJson = getSwaggerJson(path);
                return swaggerJson.toString(prettyIndent);
            }
            else {
                return getWriter(format, prettyIndent).writeValueAsString(swagger);
            }
        } 
        catch (JsonProcessingException ex) {
            throw new ServiceApiException(ex.getMessage(), ex);
        }
    }
    
    public Swagger getSwagger(String path) throws ServiceApiException {
        try {
            SwaggerScanner scanner = new SwaggerScanner(path);
            Swagger swagger = new Swagger();
            Set<Class<?>> classes = scanner.getClasses();
            if (classes != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Swagger scanning classes:");
                    for (Class<?> c : classes)
                        logger.debug("  - " + c);
                }
                SwaggerReader.read(swagger, classes);
            }
            return swagger;
        }
        catch (ClassNotFoundException ex) {
            throw new ServiceApiException(ex.getMessage(), ex);
        }
    }
    
    protected ObjectWriter getWriter(Format format, int prettyIndent) {
        ObjectMapper mapper = getMapper(format);
        if (prettyIndent > 0)
            return mapper.writer(new JacksonPrettyPrinter(prettyIndent));
        else
            return mapper.writer();        
    }
    
    protected ObjectMapper getMapper(Format format) {
        return format == Format.yaml ? Yaml.mapper() : Json.mapper();
    }
}
