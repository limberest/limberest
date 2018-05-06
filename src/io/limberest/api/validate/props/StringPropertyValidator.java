package io.limberest.api.validate.props;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.api.validate.EmailValidator;
import io.limberest.api.validate.StringValidator;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.properties.StringProperty;

public class StringPropertyValidator implements PropertyValidator<StringProperty>, ArrayPropertyValidator<StringProperty> {

    @Override
    public Result validate(JSONObject json, StringProperty property, String path, boolean strict) throws ValidationException {
        return validate(json.getString(property.getName()), property, path);
    }

    @Override
    public Result validate(JSONArray jsonArray, StringProperty property, String path) throws ValidationException {
        Result result = new Result();
        StringValidator validator = getValidator(property);
        for (int i = 0; i < jsonArray.length(); i++) {
            result.also(validator.validate(jsonArray.getString(i), path + "[" + i + "]"));
        }
        return result;
    }

    protected Result validate(String value, StringProperty property, String path) throws ValidationException {
        return getValidator(property).validate(value, path);
    }

    protected StringValidator getValidator(StringProperty property) {
        StringValidator validator;
        if ("email".equals(property.getFormat()))
            validator = new EmailValidator();
        else
            validator = new StringValidator();
        validator.setAllowableValues(property.getEnum());
        validator.setMinLength(property.getMinLength());
        validator.setMaxLength(property.getMaxLength());
        return validator;
    }
}
