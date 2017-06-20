package io.limberest.api.validate;

import static io.limberest.service.http.Status.BAD_REQUEST;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;

public class ArrayValidator<T> {

    private Integer minItems;
    public Integer getMinItems() { return minItems; }
    public void setMinItems(Integer minItems) { this.minItems = minItems; }

    private Integer maxItems;
    public Integer getMaxItems() { return maxItems; }
    public void setMaxItems(Integer maxItems) { this.maxItems = maxItems; }
    
    private PrimitiveValidator<T> validator;
    
    public ArrayValidator(PrimitiveValidator<T> validator) {
        this.validator = validator;
    }
    
    @SuppressWarnings("unchecked")
    public Result validate(Object[] items, String path) throws ValidationException {
        Result result = new Result();
        if (getMaxItems() != null && items.length > getMaxItems()) {
            String msg = path + ": array size '" + items.length + "' exceeds maximum (" + getMaxItems() + ")";
            result.also(BAD_REQUEST, msg);
        }
        if (getMinItems() != null && items.length < getMinItems()) {
            String msg = path + ": array size '" + items.length + "' is less than minimum (" + getMinItems() + ")";
            result.also(BAD_REQUEST, msg);
        }

        for (int i = 0; i < items.length; i++)
            result.also(validator.validate((T)items[i], path + "[" + i + "]"));
        
        return result;   
    }    
}
