package io.limberest.api.validate;

import static io.limberest.service.http.Status.BAD_REQUEST;

import java.math.BigDecimal;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;

public class NumberValidator extends PrimitiveValidator<BigDecimal> {

    private BigDecimal min;
    public BigDecimal getMin() { return min; }
    public void setMin(BigDecimal min) { this.min = min; }

    private BigDecimal max;
    public BigDecimal getMax() { return max; }
    public void setMax(BigDecimal max) { this.max = max; }

    private Boolean exclusiveMin;
    public Boolean isExclusiveMin() { return exclusiveMin != null && exclusiveMin; }
    public void setExclusiveMin(Boolean exclusiveMin) { this.exclusiveMin = exclusiveMin; }

    private Boolean exclusiveMax;
    public Boolean isExclusiveMax() { return exclusiveMax != null && exclusiveMax; }
    public void setExclusiveMax(Boolean exclusiveMax) { this.exclusiveMax = exclusiveMax; }
    
    private BigDecimal multipleOf;
    public BigDecimal getMultipleOf() { return multipleOf; }
    public void setMultipleOf(BigDecimal multipleOf) { this.multipleOf = multipleOf; }
    
    @Override
    public BigDecimal convert(Object value) throws ValidationException {
        if (value instanceof BigDecimal)
            return (BigDecimal)value;
        return new BigDecimal(String.valueOf(value));
    }
    
    public Result validate(BigDecimal value, String path) throws ValidationException {
        Result result = new Result();
        if (getMax() != null) {
            if (isExclusiveMax() && value.compareTo(getMax()) >= 0) {
                String msg = path + ": value '" + value + "' exceeds or equals exclusive maximum (" + getMax() + ")";
                result.also(BAD_REQUEST, msg);
            }
            else if (value.compareTo(getMax()) > 0) {
                String msg = path + ": value '" + value + "' exceeds maximum (" + getMax() + ")";
                result.also(BAD_REQUEST, msg);
            }
        }
        else if (getMin() != null) {
            if (isExclusiveMin() && value.compareTo(getMin()) <= 0) {
                String msg = path + ": value '" + value + "' is less than or equal to exclusive minimum (" + getMin() + ")";
                result.also(BAD_REQUEST, msg);
            }
            else if (value.compareTo(getMin()) < 0) {
                String msg = path + ": value '" + value + "' is less than minimum (" + getMin() + ")";
                result.also(BAD_REQUEST, msg);
            }
        }
        
        if (getMultipleOf() != null) {
            if (value.remainder(getMultipleOf()) != BigDecimal.ZERO) {
                String msg = path + ": value '" + value + "' is not a multiple of " + getMultipleOf() + ")";
                result.also(BAD_REQUEST, msg);
            }
        }
        
        return result;
    }
}
