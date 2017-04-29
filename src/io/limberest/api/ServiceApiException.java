package io.limberest.api;

public class ServiceApiException extends Exception {
    
    public ServiceApiException(String message) {
        super(message);
    }
    
    public ServiceApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
