package io.limberest.api.validate.params;

import io.limberest.api.validate.SwaggerRequest;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.parameters.PathParameter;

public class PathParameterValidator extends SimpleParameterValidator implements ParameterValidator<PathParameter> {
    
    @Override
    public Result validate(SwaggerRequest request, PathParameter parameter, Object value, boolean strict)
            throws ValidationException {
        return super.validate(request, parameter, (String)value, strict);
    }
}
