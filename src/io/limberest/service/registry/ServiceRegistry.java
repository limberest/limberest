package io.limberest.service.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.limberest.service.ResourcePath;
import io.limberest.service.Service;
import io.limberest.service.ServiceException;
import io.limberest.service.http.RestService;

/**
 * TODO: yaml option for case insensitive paths.
 */
public class ServiceRegistry {

    public static class RegistryKey {
        
        private ResourcePath path;
        public ResourcePath getPath() { return path; }
        
        private String contentType;
        public String getContentType() { return contentType; }
        
        public RegistryKey(ResourcePath path, String contentType) {
            this.path = path;
            this.contentType = contentType.toLowerCase();
        }
        public String toString() {
            return path + " (" + contentType + ")";
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof RegistryKey))
                return false;
            return toString().equals(obj.toString());
        }
        public int hashCode() {
            return toString().hashCode();
        }
    }
    
    private static ServiceRegistry instance;
    public static ServiceRegistry getInstance() {
        if (instance == null) {
            instance = new ServiceRegistry();
        }
        return instance;
    }

    private Map<RegistryKey,Class<? extends Service<?>>> services = new HashMap<>();
    public void put(RegistryKey key, Class<? extends RestService<?>> service) {
        key.contentType = key.contentType.toLowerCase();
        services.put(key, service);
    }

    public Service<?> get(RegistryKey key) throws ServiceException, InstantiationException, IllegalAccessException {
        Service<?> service = null;
        Class<? extends Service<?>> serviceClass = getServiceClass(key);
        if (serviceClass != null) {
            service = serviceClass.newInstance();
        }
        return service;
    }
    
    protected Class<? extends Service<?>> getServiceClass(RegistryKey key) {
        Class<? extends Service<?>> serviceClass = services.get(key);
        if (serviceClass == null) {
            RegistryKey pathMatch = getPathMatch(key);
            if (pathMatch != null)
                serviceClass = services.get(pathMatch);
        }
        return serviceClass;
    }
    
    /**
     * The best registered path match is determined by the longest matching sequence of path segments.
     */
    protected RegistryKey getPathMatch(RegistryKey key) {
        if (services.containsKey(key))
            return key;
        RegistryKey longestPathMatch = null;
        for (RegistryKey regKey : services.keySet()) {
            if (regKey.path.isMatch(key.path) && key.contentType.toLowerCase().startsWith(regKey.contentType)) {
               if (longestPathMatch == null || regKey.path.getSegments().length > longestPathMatch.path.getSegments().length)
                   longestPathMatch = regKey;
            }
        }
        return longestPathMatch;
    }
    
    public ResourcePath getMatchedPath(ResourcePath requestPath, String contentType) {
        RegistryKey pathMatch = getPathMatch(new RegistryKey(requestPath, contentType));
        return pathMatch == null ? null : pathMatch.getPath();
    }
    
    public Collection<Class<? extends Service<?>>> getClasses() {
        return services.values();
    }
}
