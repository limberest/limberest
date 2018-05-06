package io.limberest.api.validate.props;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.properties.DateTimeProperty;

public class DateTimePropertyValidator implements PropertyValidator<DateTimeProperty>, ArrayPropertyValidator<DateTimeProperty> {

    @Override
    public Result validate(JSONArray jsonArray, DateTimeProperty property, String path) throws ValidationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result validate(JSONObject json, DateTimeProperty property, String path, boolean strict)
            throws ValidationException {
        // TODO Auto-generated method stub
        return null;
    }

}
