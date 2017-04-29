package io.limberest.api.validate;

import java.util.List;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;

public abstract class PrimitiveValidator<T> {

    private List<String> allowableValues;
    public List<String> getAllowableValues() { return allowableValues; }
    public void setAllowableValues(List<String> values) { this.allowableValues = values; }
    
    public Result validate(T value) throws ValidationException {
        return validate(value, "");
    }
    
    public abstract Result validate(T value, String path) throws ValidationException;
    public abstract T convert(Object value) throws ValidationException;
    
    @SuppressWarnings("unchecked")
    public Result doValidate(Object value, String path) throws ValidationException {
        return validate((T)value, path);
    }
}
