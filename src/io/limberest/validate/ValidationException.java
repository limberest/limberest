package io.limberest.validate;

import io.limberest.service.ServiceException;

public class ValidationException extends ServiceException {
    
    private Result result;
    public Result getResult() { return result; }
    
    public ValidationException(Result result) {
        super(result.getStatus());
        this.result = result;
    }
    
    public ValidationException(Result result, Throwable cause) {
        super(result.getStatus(), cause);
        this.result = result;
    }
    
    public ValidationException(int code, String message) {
        this(new Result(code, message));
    }
    
    public ValidationException(int code, String message, Throwable cause) {
        this(new Result(code, message), cause);
    }
}
