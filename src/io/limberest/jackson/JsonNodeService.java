package io.limberest.jackson;

import java.io.IOException;

import javax.ws.rs.Produces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.limberest.service.ServiceException;
import io.limberest.service.http.RestService;
import io.limberest.service.http.Status;

/**
 * Generic Jackson-based service implementation.
 * Extend RestService directly for specific POJO types.
 */
@Produces("application/json")
public class JsonNodeService extends RestService<JsonNode> {

    @Override
    public JsonNode getBody(String text) throws ServiceException {
        try {
            return new ObjectMapper().readTree(text);
        }
        catch (JsonProcessingException ex) {
            throw new ServiceException(Status.BAD_REQUEST, "Invalid JSON content", ex);
        }
        catch (IOException ex) {
            throw new ServiceException(Status.INTERNAL_ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public String getText(JsonNode jsonNode, int prettyIndent) throws ServiceException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            ObjectWriter writer;
            if (prettyIndent > 0)
                writer = mapper.writer(new JacksonPrettyPrinter(prettyIndent));
            else
                writer = mapper.writer();

            return writer.writeValueAsString(jsonNode);
        }
        catch (JsonProcessingException ex) {
            throw new ServiceException(Status.INTERNAL_ERROR, ex.getMessage(), ex);
        }
    }
}
