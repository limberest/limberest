package io.limberest.api.validate;

import static io.limberest.service.http.Status.BAD_REQUEST;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;

import io.limberest.service.http.Status;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.Property;

public class DateTimeValidator extends PrimitiveValidator<Instant> {

    private Property property;

    /**
     * Parsing is done in convert().
     */
    public DateTimeValidator(Property property, String path) {
        this.property = property;
    }

    @Override
    public Result validate(Instant value, String path) throws ValidationException {
        Result result = new Result();
        if (value == null) {
            // could not parse
            String format = property instanceof DateProperty ? "date" : "date-time";
            result.also(BAD_REQUEST, path + ": invalid " + format);
            return result;
        }

        return result;
    }

    @Override
    public Instant convert(Object value) throws ValidationException {
        if (value instanceof Instant)
            return (Instant)value;
        else if (value instanceof Date)
            return ((Date)value).toInstant();
        else {
            try {
                return Instant.parse(String.valueOf(value));
            }
            catch (DateTimeParseException ex) {
                throw new ValidationException(Status.BAD_REQUEST.getCode(), ex.getMessage(), ex);
            }
        }
    }
}
