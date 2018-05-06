package io.limberest.api.validate.props;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.api.validate.DateTimeValidator;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.properties.DateProperty;

public class DatePropertyValidator implements PropertyValidator<DateProperty>, ArrayPropertyValidator<DateProperty> {

    @Override
    public Result validate(JSONObject json, DateProperty property, String path, boolean strict)
            throws ValidationException {
        return getValidator(property, path).validate(parse(json.getString(property.getName())), path);
    }

    @Override
    public Result validate(JSONArray jsonArray, DateProperty property, String path) throws ValidationException {
        Result result = new Result();
        DateTimeValidator validator = getValidator(property, path);
        for (int i = 0; i < jsonArray.length(); i++) {
            result.also(validator.validate(parse(jsonArray.getString(i)), path + "[" + i + "]"));
        }
        return result;
    }

    protected DateTimeValidator getValidator(DateProperty property, String path) {
        return new DateTimeValidator(property, path);
    }

    protected Instant parse(String value) {
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).toInstant();
        }
        catch (DateTimeParseException ex) {
            return null;
        }
    }
}
