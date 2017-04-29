package io.limberest.api.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.limberest.api.ServiceApi;
import io.limberest.api.ServiceApiException;
import io.limberest.service.ResourcePath;
import io.limberest.service.http.Request.HttpMethod;
import io.limberest.service.registry.ServiceRegistry;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;

public class SwaggerRequest {
    
    private HttpMethod method;
    public HttpMethod getMethod() { return method; }
    
    /**
     * This is the declared path that the request matched in the registry (not the actual request path).
     */
    private ResourcePath path;
    public ResourcePath getPath() { return path; }
    
    private Swagger swagger;
    public Swagger getSwagger() { return swagger; }
    
    public SwaggerRequest(HttpMethod method, ResourcePath path) throws ServiceApiException {
        this.method = method;
        this.path = ServiceRegistry.getInstance().getMatchedPath(path, "application/json");
        this.swagger = new ServiceApi().getSwagger(this.path.toString());
    }

    public Operation getOperation() {
        Path p = swagger.getPath(path.toString());
        if (p != null) {
            switch (method) {
            case GET: 
                return p.getGet();
            case POST: 
                return p.getPost();
            case PUT: 
                return p.getPut();
            case DELETE: 
                return p.getDelete();
            case PATCH: 
                return p.getPatch();
            default:
                return null;
            }
        }
            
        return null;
    }
    
    public Collection<Parameter> getParameters() {
        Operation op = getOperation();
        if (op != null) {
            return op.getParameters();
        }
        return new ArrayList<>();
    }
    
    public Map<String,Model> getDefinitions() {
        return swagger.getDefinitions();
    }
    
    public List<BodyParameter> getBodyParameters() {
        List<BodyParameter> bodyParams = new ArrayList<>();
        for (Parameter param : getParameters()) {
            if (param instanceof BodyParameter)
                bodyParams.add((BodyParameter)param);
        }
        return bodyParams;
    }
    
    public List<HeaderParameter> getHeaderParameters() {
        List<HeaderParameter> headerParams = new ArrayList<>();
        for (Parameter param : getParameters()) {
            if (param instanceof HeaderParameter)
                headerParams.add((HeaderParameter)param);
        }
        return headerParams;
    }
    
    public List<PathParameter> getPathParameters() {
        List<PathParameter> pathParams = new ArrayList<>();
        for (Parameter param : getParameters()) {
            if (param instanceof PathParameter)
                pathParams.add((PathParameter)param);
        }
        return pathParams;
    }
    
    public List<QueryParameter> getQueryParameters() {
        List<QueryParameter> queryParams = new ArrayList<>();
        for (Parameter param : getParameters()) {
            if (param instanceof QueryParameter)
                queryParams.add((QueryParameter)param);
        }
        return queryParams;
    }
}
