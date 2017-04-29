package io.limberest.api.validate.props;

import org.json.JSONObject;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.properties.Property;

@FunctionalInterface
public interface PropertyValidator<T extends Property> {

    /**
     * @param json JSON object containing the property
     * @param property Property to be validated
     * @param path for error messages
     * @param strict
     * @return validation result
     */
    public Result validate(JSONObject json, T property, String path, boolean strict) throws ValidationException;
    
    @SuppressWarnings("unchecked")
    default Result doValidate(JSONObject json, Property property, String path, boolean strict) throws ValidationException {
        return validate(json, (T)property, path, strict);
    }
}
