package io.limberest.api.validate.props;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.models.properties.Property;

public class PropertyValidators {

    private Map<Class<? extends Property>, List<PropertyValidator<? extends Property>>> propertyValidators = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Property> List<PropertyValidator<T>> getValidators(Property p) {
        List<PropertyValidator<T>> validators = new ArrayList<>();
        for (Map.Entry<Class<? extends Property>, List<PropertyValidator<? extends Property>>> entry : propertyValidators.entrySet()) {
            if (entry.getKey().isAssignableFrom(p.getClass())) {
                for (PropertyValidator<? extends Property> val : entry.getValue()) {
                    validators.add((PropertyValidator<T>)val);
                }
            }
        }
        return validators;
    }

    @SuppressWarnings("unchecked")
    public <T extends Property> List<ArrayPropertyValidator<T>> getArrayValidators(Property p) {
        List<ArrayPropertyValidator<T>> validators = new ArrayList<>();
        for (Map.Entry<Class<? extends Property>, List<PropertyValidator<? extends Property>>> entry : propertyValidators.entrySet()) {
            if (entry.getKey().isAssignableFrom(p.getClass())) {
                for (PropertyValidator<? extends Property> val : entry.getValue()) {
                    if (val instanceof ArrayPropertyValidator)
                        validators.add((ArrayPropertyValidator<T>)val);
                }
            }
        }
        return validators;
    }

    /**
     * Add a validator for a property class.
     * @param propertyClass Any Property whose class this is assignable from will use the specified validator.
     * @param validator
     */
    public <T extends Property> void add(Class<T> propertyClass, PropertyValidator<T> validator) {
        List<PropertyValidator<? extends Property>> validators = propertyValidators.get(propertyClass);
        if (validators == null) {
            validators = new ArrayList<>();
            propertyValidators.put(propertyClass, validators);
        }
        validators.add(validator);
    }

}
