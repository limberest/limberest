package io.limberest.api.validate;

import static io.limberest.service.http.Status.BAD_REQUEST;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;

public class DateValidator extends StringValidator {

    @Override
    public Result validate(String value, String path) throws ValidationException {
        Result result = new Result();
        try {
            LocalDate.parse(value);
        }
        catch (DateTimeParseException ex) {
            String msg = path + ": invalid date";
            result.also(BAD_REQUEST, msg);
        }
        return result;
    }
}
