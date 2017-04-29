package io.limberest.api.validate.props;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.api.validate.NumberValidator;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.properties.AbstractNumericProperty;

public class NumberPropertyValidator
        implements PropertyValidator<AbstractNumericProperty>, ArrayPropertyValidator<AbstractNumericProperty> {

    @Override
    public Result validate(JSONObject json, AbstractNumericProperty property, String path, boolean strict) throws ValidationException {
        return validate(json.getBigDecimal(property.getName()), property, path);
    }

    @Override
    public Result validate(JSONArray jsonArray, AbstractNumericProperty property, String path) throws ValidationException {
        Result result = new Result();
        NumberValidator validator = getValidator(property);
        for (int i = 0; i < jsonArray.length(); i++) {
            result.also(validator.validate(jsonArray.getBigDecimal(i), path + "[" + i + "]"));
        }
        return result;
    }
    
    protected Result validate(BigDecimal value, AbstractNumericProperty property, String path) throws ValidationException {
        return getValidator(property).validate(value, path);
    }
    
    protected NumberValidator getValidator(AbstractNumericProperty property) {
        NumberValidator numVal = new NumberValidator();
        numVal.setMax(property.getMaximum());
        numVal.setMin(property.getMinimum());
        numVal.setExclusiveMax(property.getExclusiveMaximum());
        numVal.setMultipleOf(property.getMultipleOf());
        return numVal;
    }
}
