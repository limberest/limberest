package io.limberest.api.validate.params;

import io.limberest.api.validate.SwaggerRequest;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.parameters.HeaderParameter;

public class HeaderParameterValidator extends SimpleParameterValidator implements ParameterValidator<HeaderParameter> {
    
    @Override
    public Result validate(SwaggerRequest request, HeaderParameter parameter, Object value, boolean strict) 
            throws ValidationException {
        return super.validate(request, parameter, (String)value, strict);
    }
}