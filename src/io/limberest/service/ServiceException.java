package io.limberest.service;

import io.limberest.service.http.Status;

public class ServiceException extends Exception {

    private Status status;
    public int getCode() { return status.getCode(); }
    
    public ServiceException(Status status) {
        super(status.getMessage());
        this.status = status;
    }
    
    public ServiceException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public ServiceException(Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
    
    public ServiceException(Status status, Throwable cause) {
        super(status.getMessage(), cause);
        this.status = status;
    }
    
}
