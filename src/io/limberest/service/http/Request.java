package io.limberest.service.http;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import io.limberest.service.Query;
import io.limberest.service.ResourcePath;

/**
 * limberest service request
 */
public class Request<T> {
    
    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH
    }

    private HttpMethod method;
    public HttpMethod getMethod() { return method; }

    private ResourcePath path;
    public ResourcePath getPath() { return path; }
    
    private Query query;
    public Query getQuery() { return query; }
    
    private URL base;
    public URL getBase() { return base; }
    
    private Map<String,String> headers = new HashMap<>();
    public Map<String,String> getHeaders() { return headers; }
    
    private String user;
    /**
     * Authenticated user.
     */
    protected String getUser() { return user; }
    void setUser(String user) { this.user = user; }
    
    private Predicate<String> userRolePredicate;
    protected Predicate<String> getUserRolePredicate() { return userRolePredicate; }
    void setUserRolePredicate(Predicate<String> p) { this.userRolePredicate = p; }
    
    private String text;
    String getText() { return text; }
    void setText(String text) { this.text = text; }
    
    private T body;
    public T getBody() { return body; }
    void setBody(T body) { this.body = body; }

    Request(HttpMethod method, URL base, ResourcePath path, Query query, Map<String,String> headers) {
        this.method = method;
        this.base = base;
        this.path = path;
        this.query = query;
        this.headers = headers;
    }
    
    public String toString() {
        return (user == null ? "" : user + "-> ") + method + " " + path + "?" + query;
    }
    
}
