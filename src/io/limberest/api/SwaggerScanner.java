package io.limberest.api;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import io.limberest.config.LimberestConfig;
import io.limberest.config.LimberestConfig.Settings;
import io.limberest.service.registry.ServiceRegistry;
import io.swagger.annotations.Api;

public class SwaggerScanner {
    private String servicePath; // must begin with '/'

    public SwaggerScanner() {
        this("/");
    }

    public SwaggerScanner(String servicePath) {
        this.servicePath = servicePath;
    }

    public Set<Class<?>> getClasses() throws ClassNotFoundException {

        Set<Class<?>> classes = new HashSet<>();
        
        List<String> apiDefinitionClasses = getApiDefinitionClasses();
        if (apiDefinitionClasses == null) {
            classes.add(DefaultSwaggerDefinition.class);
        }
        else {
            for (String apiDefClass : apiDefinitionClasses) {
                classes.add(Class.forName(apiDefClass));
            }
        }

        // service classes
        ServiceRegistry.getInstance().getClasses().stream().filter(c -> {
            Api apiAnnotation = c.getAnnotation(Api.class);
            if (apiAnnotation != null) {
                if ("/".equals(servicePath)) {
                    return true;
                }
                else {
                    String path = c.getSimpleName();
                    Path pathAnnotation = c.getAnnotation(Path.class);
                    if (pathAnnotation != null && pathAnnotation.value() != null)
                        path = pathAnnotation.value();
                    return path.startsWith(servicePath) || ("/" + path).startsWith(servicePath);
                }
            }
            return false;
        }).forEach(c -> classes.add(c));
        
        return classes;
    }
    
    protected List<String> getApiDefinitionClasses() {
        // swagger definition
        List<String> apiDefinitionClasses = null;
        Settings settings = LimberestConfig.getSettings();
        Map<?,?> api = settings.getMap("api");
        if (api != null)
            apiDefinitionClasses = settings.getStringList("definitionClasses", api);
        return apiDefinitionClasses;
    }

}
