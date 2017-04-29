package io.limberest.api.validate.props;

import org.json.JSONArray;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.properties.Property;

@FunctionalInterface
public interface ArrayPropertyValidator<T extends Property> {

    public Result validate(JSONArray jsonArray, T property, String path) throws ValidationException;
 
    @SuppressWarnings("unchecked")
    default Result doValidate(JSONArray jsonArray, Property property, String path) throws ValidationException {
        return validate(jsonArray, (T)property, path);
    }
    
}
