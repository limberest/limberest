package io.limberest.service.http;

import java.util.HashMap;
import java.util.Map;

/**
 * limberest service response
 */
public class Response<T> {

    private Map<String,String> headers = new HashMap<>();
    public Map<String,String> getHeaders() { return headers; }
    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }
    
    private T body;
    public T getBody() { return body; }
    
    private String text;
    public String getText() { return text; }
    void setText(String text) { this.text = text; }
    
    private Status status;
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    /**
     * Overrides default status message.
     */
    private String message;
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public void setStatus(Status status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public Response() {
    }
    
    public Response(Status status) {
        this.status = status;
    }
    
    public Response(T body) {
        this.body = body;
    }
    
    public Response(Status status, T body) {
        this.status = status;
        this.body = body;
    }    
}
