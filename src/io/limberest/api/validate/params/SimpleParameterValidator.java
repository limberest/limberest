package io.limberest.api.validate.params;

import static io.limberest.service.http.Status.BAD_REQUEST;
import static io.limberest.service.http.Status.INTERNAL_ERROR;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.limberest.api.validate.ArrayValidator;
import io.limberest.api.validate.NumberValidator;
import io.limberest.api.validate.PrimitiveValidator;
import io.limberest.api.validate.StringValidator;
import io.limberest.api.validate.SwaggerRequest;
import io.limberest.validate.Result;
import io.limberest.validate.ValidationException;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.util.PrimitiveType;

/**
 * Validator base for Parameters whose values can be expressed as
 * simple strings (eg: path, query and header parameters).
 * Currently only primitive dataTypes are supported.
 * TODO: check handling of date and dateTime
 */
public class SimpleParameterValidator {

    private static final Logger logger = LoggerFactory.getLogger(SimpleParameterValidator.class);
    
    protected Result validate(SwaggerRequest request, Parameter param, String value, boolean strict) throws ValidationException {
        Result result = new Result();
        if (value == null || value.isEmpty()) {
            if (param.getRequired())
                result.also(BAD_REQUEST, "Missing required param: " + param.getName());
        }
        else if (param instanceof SerializableParameter) {
            SerializableParameter p = (SerializableParameter) param;
            String dataType = p.getType();
            if (dataType != null) {
                PrimitiveType pt = null;
                if ("array".equals(dataType)) {
                    if (p.getItems() != null)
                        pt = PrimitiveType.fromName(p.getItems().getType());
                }
                else {
                    pt = PrimitiveType.fromName(dataType);
                }
                if (pt != null) {
                    Class<?> keyClass = pt.getKeyClass();
                    PrimitiveValidator<?> validator = getValidator(keyClass, p);
                    if (p.getItems() != null) {
                        // array of values
                        ArrayValidator<?> arrayValidator = new ArrayValidator<>(validator);
                        arrayValidator.setMinItems(p.getMinItems());
                        arrayValidator.setMaxItems(p.getMaxItems());
                        String[] values = value.split(","); // TODO non-csv (p.collectionFormat)
                        Object[] objs = new Object[values.length];
                        for (int i = 0; i < values.length; i++) { 
                            Object obj = parse(keyClass, p, values[i], i, result);
                            objs[i] = validator.convert(obj);
                        }
                        result.also(arrayValidator.validate(objs, param.getName()));
                    }
                    else {
                        if (Number.class.isAssignableFrom(keyClass) && value != null && (value.indexOf('>') >= 0 || value.indexOf('<') >= 0)) {
                            // allow comparison operators (>1931, <=1945, >=1930<1940)
                            String[] vals = value.split("[<>]=?");
                            for (int i = 0; i < vals.length; i++) {
                                if (i > 0 || !vals[i].isEmpty()) {
                                    Object obj = parse(keyClass, p, vals[i], -1, result);
                                    if (obj != null)
                                        result.also(validator.doValidate(validator.convert(obj), param.getName()));
                                }
                            }
                        }
                        else {
                            Object obj = parse(keyClass, p, value, -1, result);
                            if (obj != null)
                                result.also(validator.doValidate(validator.convert(obj), param.getName()));
                        }
                    }
                }
            }
        }
        return result;
    }
    
    protected Object parse(Class<?> keyClass, SerializableParameter param, String value, int index, Result result) 
            throws ValidationException {
        try {
            // parse by instantiating
            Constructor<?> c = keyClass.getConstructor(String.class);
            return c.newInstance(value);
        }
        catch (IllegalArgumentException ex) {
            logger.debug(ex.getMessage(), ex);
            result.also(BAD_REQUEST, invalidTypeMessage(param, value, index));
            return null;
        }
        catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof IllegalArgumentException) {
                logger.debug(ex.getMessage(), ex);
                result.also(BAD_REQUEST, invalidTypeMessage(param, value, index));
                return null;
            }
            else {
                throw new ValidationException(INTERNAL_ERROR.getCode(), ex.getMessage(), ex);
            }
        }
        catch (Exception ex) {
            throw new ValidationException(INTERNAL_ERROR.getCode(), ex.getMessage(), ex);
        }
    }
    
    protected String invalidTypeMessage(SerializableParameter param, String value, int index) {
        return "invalid " + param.getType() + " value for " + param.getIn() + " param " + param.getName()
            + (index == -1 ? "" : "[" + index + "]") + ": " + value;
    }
    
    protected PrimitiveValidator<?> getValidator(Class<?> keyClass, SerializableParameter param) {
        PrimitiveValidator<?> validator;
        
        if (Number.class.isAssignableFrom(keyClass)) {
            NumberValidator numVal = new NumberValidator();
            numVal.setMax(param.getMaximum());
            numVal.setMin(param.getMinimum());
            validator = numVal;
        }
        else {
            StringValidator stringVal = new StringValidator();
            stringVal.setMinLength(param.getMinLength());
            stringVal.setMaxLength(param.getMaxLength());
            validator = stringVal; 
        }
        
        validator.setAllowableValues(param.getEnum());
        
        return validator;
        
    }
}
