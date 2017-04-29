package io.limberest.api.validate.params;

import io.limberest.api.validate.SwaggerRequest;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.parameters.Parameter;

@FunctionalInterface
public interface ParameterValidator<T extends Parameter> {

    /**
     * @param swaggerRequest holding the model for this swagger context
     * @param parameter Parameter to be validated
     * @param value Parameter value
     * @param strict
     * @return validation result
     */
    public Result validate(SwaggerRequest swaggerRequest, T parameter, Object value, boolean strict) throws ValidationException;
    
    @SuppressWarnings("unchecked")
    default Result doValidate(SwaggerRequest swaggerRequest, Parameter parameter, Object value, boolean strict) throws ValidationException {
        return validate(swaggerRequest, (T)parameter, value, strict);
    }
}