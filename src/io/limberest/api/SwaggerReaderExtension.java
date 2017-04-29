package io.limberest.api;

import java.lang.reflect.Method;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.apache.commons.lang3.StringUtils;

import io.limberest.service.http.RestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.Operation;
import io.swagger.servlet.ReaderContext;
import io.swagger.servlet.extensions.ReaderExtension;
import io.swagger.servlet.extensions.ServletReaderExtension;
import io.swagger.util.PathUtils;
import io.swagger.util.ReflectionUtils;

public class SwaggerReaderExtension extends ServletReaderExtension implements ReaderExtension {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isReadable(ReaderContext context) {
        return true;
    }

    @Override
    public String getHttpMethod(ReaderContext context, Method method) {
        ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
        if (apiOperation != null && apiOperation.httpMethod() != null && !apiOperation.httpMethod().isEmpty()) {
            return apiOperation.httpMethod();
        }
        else {
            // JAX-RS annotations
            GET get = ReflectionUtils.getAnnotation(method, GET.class);
            if (get != null)
                return "get";
            PUT put = ReflectionUtils.getAnnotation(method, PUT.class);
            if (put != null) {
                if (method.getName().equalsIgnoreCase("patch"))
                    return "patch";
                return "put";
            }
            POST post = ReflectionUtils.getAnnotation(method, POST.class);
            if (post != null)
                return "post";
            DELETE delete = ReflectionUtils.getAnnotation(method, DELETE.class);
            if (delete != null)
                return "delete";

            // return our method name
            if (RestService.class.isAssignableFrom(method.getDeclaringClass()))
                return method.getName();

            return null;
        }
    }

    @Override
    public String getPath(ReaderContext context, Method method) {
        String p = null;
        Api apiAnnotation = context.getCls().getAnnotation(Api.class);
        ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
        String operationPath = apiOperation == null ? null : apiOperation.nickname();
        if (operationPath != null && !operationPath.isEmpty()) {
            // same logic as ServletReaderExtension
            p = PathUtils.collectPath(context.getParentPath(),
                    apiAnnotation == null ? null : apiAnnotation.value(), operationPath);
        }
        else {
            // try JAX-RS annotations
            Path parentPath = ReflectionUtils.getAnnotation(method.getDeclaringClass(), Path.class);
            if (parentPath != null && parentPath.value() != null && !parentPath.value().isEmpty()) {
                p = parentPath.value();
            }
            Path path = ReflectionUtils.getAnnotation(method, Path.class);
            if (path != null && path.value() != null && !path.value().isEmpty()) {
                if (p == null)
                    p = path.value();
                else {
                    if (path.value().startsWith("/"))
                        p += path.value();
                    else
                        p = p + "/" + path.value();
                }
            }
        }
        return p;
    }

    public void applyTags(ReaderContext context, Operation operation, Method method) {
        super.applyTags(context, operation, method);
        
        Class<?> declaringClass = method.getDeclaringClass();
        Api apiAnnotation = declaringClass.getAnnotation(Api.class);
        if (apiAnnotation != null && apiAnnotation.value() != null && !apiAnnotation.value().isEmpty()) {
            operation.addTag(apiAnnotation.value());
        }
    }

    /**
     * Implement to allow loading of custom types nonstandard ClassLoader.
     */
    @Override
    public void applyImplicitParameters(ReaderContext context, Operation operation, Method method) {
        super.applyImplicitParameters(context, operation, method);
    }
    
    @Override
    public void applyOperationId(Operation operation, Method method) {
        ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
        if (apiOperation != null && StringUtils.isNotBlank(apiOperation.nickname())) {
            operation.operationId(apiOperation.nickname());
        } 
    }    
}
