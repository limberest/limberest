package io.limberest.api.validate.params;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.models.parameters.Parameter;

public class ParameterValidators {

    private Map<Class<? extends Parameter>, List<ParameterValidator<? extends Parameter>>> parameterValidators = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T extends Parameter> List<ParameterValidator<T>> getValidators(Parameter p) {
        List<ParameterValidator<T>> validators = new ArrayList<>();
        for (Map.Entry<Class<? extends Parameter>, List<ParameterValidator<? extends Parameter>>> entry : parameterValidators.entrySet()) {
            if (entry.getKey().isAssignableFrom(p.getClass())) {
                for (ParameterValidator<? extends Parameter> val : (List<ParameterValidator<? extends Parameter>>)entry.getValue()) {
                    validators.add((ParameterValidator<T>)val);
                }
            }
        }
        return validators;
    }

    /**
     * Add a validator for a parameter type.
     * @param parameterType Any Parameter whose class this is assignable from will use the specified validator.
     * @param validator
     */
    public <T extends Parameter> void add(Class<T> parameterType, ParameterValidator<T> validator) {
        List<ParameterValidator<? extends Parameter>> validators = parameterValidators.get(parameterType);
        if (validators == null) {
            validators = new ArrayList<>();
            parameterValidators.put(parameterType, validators);
        }
        validators.add(validator);
    }
}
