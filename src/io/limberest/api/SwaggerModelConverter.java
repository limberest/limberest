package io.limberest.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Iterator;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.type.SimpleType;

import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.Model;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.EmailProperty;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;

public class SwaggerModelConverter extends ModelResolver {

    public SwaggerModelConverter() {
        super(Json.mapper());
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations,
            Iterator<ModelConverter> chain) {
        if (type instanceof SimpleType) {
            SimpleType simpleType = (SimpleType)type;
            String className = simpleType.getRawClass() == null ? null : simpleType.getRawClass().getName();
            if (Instant.class.getName().equals(className)) {
                return new DateTimeProperty();
            }
            else if (LocalDate.class.getName().equals(className)) {
                return new DateProperty();
            }
            else if (annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().getName().equals("javax.validation.constraints.Email"))
                        return new EmailProperty();
                }
            }

        }
        return super.resolveProperty(type, context, annotations, chain);
    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        return super.resolve(type, context, chain);
    }

    @Override
    protected boolean shouldIgnoreClass(Type type) {
        if (type instanceof SimpleType && JSONObject.class.equals(((SimpleType)type).getRawClass())) {
            return true;
        }
        return super.shouldIgnoreClass(type);
    }
}
