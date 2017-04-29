package io.limberest.api.validate.params;

import io.limberest.api.validate.SwaggerRequest;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.parameters.QueryParameter;

public class QueryParameterValidator extends SimpleParameterValidator implements ParameterValidator<QueryParameter> {
    
    @Override
    public Result validate(SwaggerRequest request, QueryParameter parameter, Object value, boolean strict) 
            throws ValidationException {
        return super.validate(request, parameter, (String)value, strict);
    }
}