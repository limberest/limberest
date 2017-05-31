package io.limberest.validate;

import io.limberest.service.http.Request;

@FunctionalInterface
public interface Validator<T> {
    
    public Result validate(Request<T> request) throws ValidationException;

}
