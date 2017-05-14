package io.limberest.api.validate;

import static io.limberest.service.http.Status.NOT_FOUND;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import io.limberest.api.ServiceApiException;
import io.limberest.api.validate.params.BodyParameterValidator;
import io.limberest.api.validate.params.HeaderParameterValidator;
import io.limberest.api.validate.params.ParameterValidator;
import io.limberest.api.validate.params.ParameterValidators;
import io.limberest.api.validate.params.PathParameterValidator;
import io.limberest.api.validate.params.QueryParameterValidator;
import io.limberest.api.validate.props.NumberPropertyValidator;
import io.limberest.api.validate.props.PropertyValidator;
import io.limberest.api.validate.props.PropertyValidators;
import io.limberest.api.validate.props.StringPropertyValidator;
import io.limberest.service.Query;
import io.limberest.service.ResourcePath;
import io.limberest.service.http.Request;
import io.limberest.service.http.Status;
import io.limberest.util.ExecutionTimer;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.limberest.validate.Validator;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.AbstractNumericProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

public class SwaggerValidator implements Validator<JSONObject> {
    
    public SwaggerValidator() {
        addDefaultValidators();
    }

    private PropertyValidators propertyValidators = new PropertyValidators();
    private ParameterValidators parameterValidators = new ParameterValidators();
    
    public SwaggerValidator(ParameterValidators parameterValidators, PropertyValidators propertyValidators) {
        this.parameterValidators = parameterValidators;
        this.propertyValidators = propertyValidators;
    }
    
    protected void addDefaultValidators() {
        addValidator(StringProperty.class, new StringPropertyValidator());
        addValidator(AbstractNumericProperty.class, new NumberPropertyValidator());
        addValidator(PathParameter.class, new PathParameterValidator());
        addValidator(QueryParameter.class, new QueryParameterValidator());
        addValidator(HeaderParameter.class, new HeaderParameterValidator());
        addValidator(BodyParameter.class, new BodyParameterValidator(propertyValidators));
    }

    private SwaggerRequest swaggerRequest;

    @Override
    public Result validate(Request<JSONObject> request) throws ValidationException {
        return validate(request, false);
    }
    
    public Result validate(Request<JSONObject> request, boolean strict) throws ValidationException {
        ExecutionTimer timer = new ExecutionTimer(true);
        try {
            Result result = new Result();

            swaggerRequest = new SwaggerRequest(request.getMethod(), request.getPath());
            
            result.also(validatePath(request.getPath(), strict));
            result.also(validateQuery(request.getQuery(), strict));
            result.also(validateHeaders(request.getHeaders(), strict));
            result.also(validateBody(request.getBody(), strict));
            return result;
        }
        catch (ServiceApiException ex) {
            throw new ValidationException(NOT_FOUND.getCode(), ex.getMessage(), ex);
        }
        finally {
            timer.log("SwaggerValidator: validate:");
        }
    }
    
    public Result validatePath(ResourcePath path, boolean strict) throws ValidationException {
        Result result = new Result();
        ResourcePath swaggerPath = swaggerRequest.getPath();
        if (strict) {
            // validate path segments
            if (swaggerPath.getSegments().length < path.getSegments().length) {
                String unknownPath = "";
                for (int i = swaggerPath.getSegments().length; i < path.getSegments().length; i++)
                    unknownPath += "/" + path.getSegment(i);
                result.also(Status.BAD_REQUEST, "Unknown path suffix: " + unknownPath);
            }
        }
        List<PathParameter> pathParams = swaggerRequest.getPathParameters();
        for (int i = 0; i < pathParams.size(); i++) {
            PathParameter pathParam = pathParams.get(i);
            for (ParameterValidator<? extends Parameter> validator : parameterValidators.getValidators(pathParam)) {
                result.also(validator.doValidate(swaggerRequest, pathParam, path.getSegment(i), strict));
            }
        }
        return result;
    }

    // TODO honor strict
    public Result validateQuery(Query query, boolean strict) throws ValidationException {
        Result result = new Result();
        for (QueryParameter queryParam : swaggerRequest.getQueryParameters()) {
            for (ParameterValidator<? extends Parameter> validator : parameterValidators.getValidators(queryParam)) {
                result.also(validator.doValidate(swaggerRequest, queryParam, query.getFilter(queryParam.getName()), strict));
            }
        }
        return result;
    }

    public Result validateHeaders(Map<String,String> headers, boolean strict) throws ValidationException {
        Result result = new Result();
        for (HeaderParameter headerParam : swaggerRequest.getHeaderParameters()) {
            for (ParameterValidator<? extends Parameter> validator : parameterValidators.getValidators(headerParam)) {
                String value = headers.get(headerParam.getName());
                if (value == null)
                    value = headers.get(headerParam.getName().toLowerCase());
                result.also(validator.doValidate(swaggerRequest, headerParam, value, strict));
            }
        }
        return result;
    }

    public Result validateBody(JSONObject body, boolean strict) throws ValidationException {
        Result result = new Result();
        for (BodyParameter bodyParam : swaggerRequest.getBodyParameters()) {
            for (ParameterValidator<? extends Parameter> validator : parameterValidators.getValidators(bodyParam)) {
                result.also(validator.doValidate(swaggerRequest, bodyParam, body, strict));
            }
        }
        return result;
    }

    /**
     * Add a validator for a parameter type.
     * @param parameterType Any Parameter whose class this is assignable from will use the specified validator.
     * @param validator
     */
    public <T extends Parameter> void addValidator(Class<T> parameterType, ParameterValidator<T> validator) {
        parameterValidators.add(parameterType, validator);
    }
    
    /**
     * Add a validator for a property class.
     * @param propertyClass Any Property whose class this is assignable from will use the specified validator.
     * @param validator
     */
    public <T extends Property> void addValidator(Class<T> propertyClass, PropertyValidator<T> validator) {
        propertyValidators.add(propertyClass, validator);
    }    
    
}
