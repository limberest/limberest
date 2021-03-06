package io.limberest.api.validate.props;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.api.validate.DateTimeValidator;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.properties.DateTimeProperty;

public class DateTimePropertyValidator implements PropertyValidator<DateTimeProperty>, ArrayPropertyValidator<DateTimeProperty> {

    @Override
    public Result validate(JSONObject json, DateTimeProperty property, String path, boolean strict)
            throws ValidationException {
        return getValidator(property, path).validate(parse(json.getString(property.getName())), path);
    }

    @Override
    public Result validate(JSONArray jsonArray, DateTimeProperty property, String path) throws ValidationException {
        Result result = new Result();
        DateTimeValidator validator = getValidator(property, path);
        for (int i = 0; i < jsonArray.length(); i++) {
            result.also(validator.validate(parse(jsonArray.getString(i)), path + "[" + i + "]"));
        }
        return result;
    }

    protected DateTimeValidator getValidator(DateTimeProperty property, String path) {
        return new DateTimeValidator(property, path);
    }

    protected Instant parse(String value) {
        try {
            return Instant.parse(value);
        }
        catch (DateTimeParseException ex) {
            return null;
        }
    }
}
