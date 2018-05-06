package io.limberest.api.validate;

import static io.limberest.service.http.Status.BAD_REQUEST;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;

public class DateTimeValidator extends StringValidator {

    @Override
    public Result validate(String value, String path) throws ValidationException {
        Result result = new Result();
        try {
            Instant.parse(value);
        }
        catch (DateTimeParseException ex) {
            String msg = path + ": invalid date-time";
            result.also(BAD_REQUEST, msg);
        }
        return result;
    }

}
