package io.limberest.api.validate.params;

import static io.limberest.service.http.Status.BAD_REQUEST;
import static io.limberest.service.http.Status.OK;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

import io.limberest.api.validate.SwaggerRequest;
import io.limberest.api.validate.props.ArrayPropertyValidator;
import io.limberest.api.validate.props.PropertyValidator;
import io.limberest.api.validate.props.PropertyValidators;
import io.limberest.service.http.Status;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.PrimitiveType;
import io.swagger.util.ReflectionUtils;

public class BodyParameterValidator implements ParameterValidator<BodyParameter> {
    
    private SwaggerRequest swaggerRequest;
    private PropertyValidators propertyValidators;
    
    public BodyParameterValidator(PropertyValidators propertyValidators) {
        this.propertyValidators = propertyValidators;
    }
    
    @Override
    public Result validate(SwaggerRequest request, BodyParameter parameter, Object value, boolean strict) throws ValidationException {
        this.swaggerRequest = request;
        return validate(parameter, (JSONObject)value, strict);
    }

    public Result validate(BodyParameter bodyParam, JSONObject body, boolean strict) throws ValidationException {
        Result result = new Result();
        if (body == null) {
            if (bodyParam.getRequired())
                result.also(Status.BAD_REQUEST, "Missing required body parameter: " + bodyParam.getName());
        }
        else {
            Model model = bodyParam.getSchema();
            if (model instanceof RefModel) {
                Model bodyModel = swaggerRequest.getDefinitions().get(((RefModel)model).getSimpleRef());
                if (bodyModel != null)
                    result.also(validate(body, bodyModel, "", strict));
            }
        }
        return result;
    }
    
    /**
     * Validate a json object against its swagger model.
     * @param json object
     * @param model swagger model
     * @param path cumulative path for message building
     * @param strict
     * @return combined status
     */
    private Result validate(JSONObject json, Model model, String path, boolean strict) throws ValidationException {
        Result result = new Result();
        for (Property prop : model.getProperties().values()) {
            result.also(validate(json, prop, path, strict));
        }
        if (strict) {
            result.also(validateExtraneous(json, model, path));
        }
        return result;
    }

    /**
     * Validate a json object against a swagger property.
     * @param json the containing JSONObject
     * @param prop swagger property
     * @param path cumulative path for message building
     * @param strict
     * @return status
     */
    protected Result validate(JSONObject json, Property prop, String path, boolean strict) throws ValidationException {
        Result result = new Result();
        String name = prop.getName();
        path += path == null || path.isEmpty() ? name : "." + name;
        
        if (json.has(name)) {
            if (prop.getReadOnly() != null && prop.getReadOnly()) {
                result.also(BAD_REQUEST, path + " is read-only");
            }
            else {
                Result typeMatch = matchType(json.get(name), prop, path);
                if (typeMatch.getStatus().getCode() == OK.getCode()) {
                    if (prop instanceof RefProperty) {
                        JSONObject obj = json.getJSONObject(name);
                        String ref = ((RefProperty)prop).getSimpleRef();
                        Model model = swaggerRequest.getDefinitions().get(ref);
                        result.also(validate(obj, model, path, strict));
                    }
                    else if (prop instanceof ArrayProperty) {
                        Property itemsProp = ((ArrayProperty)prop).getItems();
                        if (itemsProp != null)
                            result.also(validate(json.getJSONArray(name), itemsProp, path, strict));
                    }
                    else {
                        for (PropertyValidator<? extends Property> validator : propertyValidators.getValidators(prop)) {
                            result.also(validator.doValidate(json, prop, path, strict));
                        }
                    }
                }
                else {
                    result.also(typeMatch); 
                }
            }
        }
        else if (prop.getRequired()) {
            String msg = path + " is required";
            result.also(BAD_REQUEST, msg);
        }
        
        return result;
    }

    protected Result validate(JSONArray jsonArray, Property prop, String path, boolean strict) throws ValidationException {
        Result result = new Result();
        if (prop instanceof RefProperty) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String itemPath = path + "[" + i + "]";
                JSONObject obj = jsonArray.getJSONObject(i);
                String ref = ((RefProperty)prop).getSimpleRef();
                Model model = swaggerRequest.getDefinitions().get(ref);
                result.also(validate(obj, model, itemPath, strict));
            }
        }
        else if (prop instanceof ArrayProperty) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String itemPath = path + "[" + i + "]";
                Property itemsProp = ((ArrayProperty)prop).getItems();
                if (itemsProp != null)
                    result.also(validate(jsonArray.getJSONArray(i), itemsProp, itemPath, strict));
            }
        }
        else {
            for (ArrayPropertyValidator<? extends Property> validator : propertyValidators.getArrayValidators(prop)) {
                result.also(validator.doValidate(jsonArray, prop, path));
            }
        }
        return result;
    }
    
    private Result validateExtraneous(JSONObject json, Model model, String path) throws ValidationException {
        Result result = new Result();
        for (String name : JSONObject.getNames(json)) {
            if (!model.getProperties().containsKey(name)) {
                String msg = (path == null || path.isEmpty() ? name : path + "." + name) + ": unknown property";
                result.also(BAD_REQUEST, msg);
            }
        }
        return result;
    }
    
    protected Result matchType(Object obj, Property prop, String path) throws ValidationException {
        String expectedType = prop.getType();
        if ("array".equals(expectedType)) {
            expectedType = JSONArray.class.getName();
        }
        else if ("ref".equals(expectedType) || "object".equals(expectedType)) {
            expectedType = JSONObject.class.getName();
        }
        else {
            PrimitiveType type = PrimitiveType.fromName(prop.getType());
            if (type != null) {
                expectedType = type.getKeyClass().getName();
                if (BigDecimal.class.getName().equals(expectedType))
                    expectedType = Double.class.getName();
            }
            else {
                Type refType = ReflectionUtils.typeFromString(prop.getType());
                if (refType != null)
                    expectedType = refType.getTypeName();
            }
        }
            
        String foundType = obj.getClass().getName();
        boolean match = foundType.equals(expectedType);
        if (!match) {
            // forgive rounded numeric types
            if (BigDecimal.class.getName().equals(expectedType)) {
                if (foundType.equals(Double.class.getName()) || foundType.equals(Integer.class.getName()))
                    match = true;
            }
            else if (Double.class.getName().equals(expectedType)) {
                if (foundType.equals(Integer.class.getName()))
                    match = true;
            }
            else if (Integer.class.getName().equals(expectedType)) {
                if (foundType.equals(Long.class.getName()) && "int64".equals(prop.getFormat()))
                    match = true;
            }
        }
        
        if (match)
            return new Result();
        else
            return new Result(BAD_REQUEST, path + ": expected " + expectedType + " but found " + foundType);
    }

}
