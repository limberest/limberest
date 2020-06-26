package io.limberest.api.validate;

import static io.limberest.service.http.Status.BAD_REQUEST;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;

public class StringValidator extends PrimitiveValidator<String> {

    private Integer minLength;
    public Integer getMinLength() { return minLength; }
    public void setMinLength(Integer minLength) { this.minLength = minLength; }

    private Integer maxLength;
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }

    @Override
    public String convert(Object value) throws ValidationException {
        return String.valueOf(value);
    }

    @Override
    public Result validate(String value, String path) throws ValidationException {
        Result result = new Result();

        if (getAllowableValues() != null) {
            if (!getAllowableValues().contains(value)) {
                String msg = path + ": value '" + value + "' is not allowed";
                result.also(BAD_REQUEST, msg);
            }
        }

        if (getMinLength() != null && value.length() < getMinLength()) {
            String msg = path + ": value '" + value + "' does not meet minimum length (" + getMinLength() + ")";
            result.also(BAD_REQUEST, msg);
        }
        if (getMaxLength() != null && value.length() > getMaxLength()) {
            String msg = path + ": value '" + value + "' exceeds maximum length (" + getMaxLength() + ")";
            result.also(BAD_REQUEST, msg);
        }

        return result;
    }

}
