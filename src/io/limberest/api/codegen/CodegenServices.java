package io.limberest.api.codegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.codegen.DefaultCodegen;

/**
 * Every path equates to one service unless squashed.
 */
public class CodegenServices {

    protected Map<String,Service> services;
    protected boolean squash;

    public CodegenServices() {
        this.services = new LinkedHashMap<>();
    }

    /**
     * Returns true if existing method should be removed from operations.
     * (never the case except squashed with pre-existing service method).
     */
    public boolean add(String path, String tag, String methodName) {
        // String serviceName = toServiceName(tag, path);
        Service service = services.get(path);
        if (service == null) {
            // check this service name on different (shorter) path
            Service existing = squash ? null : forTag(tag);
            if (existing == null) {
                service = new Service(tag, path);
            }
            else {
                // need to create a unique Service for longer path
                String extra = path.substring(existing.path.length());
                service = new Service(tag + toClassNamePart(extra), path);
            }
        }
        services.put(path, service);
        if (squash) {
            Method method = service.getMethod(methodName);
            if (method != null) {
                method.path = path;  // update to longer path
                return true;
            }
        }
        service.methods.add(new Method(methodName, path));
        return false;
    }

    Service forTag(String tag) {
        for (Service service : services.values()) {
            if (service.tag.equals(tag))
                return service;
        }
        return null;
    }

    Service forPath(String path) {
        return services.get(path);
    }

    static String toClassNamePart(String in) {
        String out = in.replaceAll("[/{}]", "");
        out = Character.toUpperCase(out.charAt(0)) + out.substring(1);
        StringBuilder sb = new StringBuilder(out.length());
        for (char c : out.toCharArray()) {
            if(!Character.isJavaIdentifierPart(c)) {
                sb.append("_");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public class Service {
        protected String tag;
        protected String path;
        protected List<Method> methods;

        Service(String tag, String path) {
            this.tag = tag;
            this.path = path;
            this.methods = new ArrayList<>();
        }

        Method getMethod(String name) {
            for (Method method : methods) {
                if (method.name.equals(name))
                    return method;
            }
            return null;
        }

        /**
         * Generated class name
         */
        String getName() {
            String serviceName = tag;
            if (serviceName.length() == 0 || serviceName.equals("Default")) {
                // use path segment, which is more meaningful
                serviceName = path.replace('{', '_').replaceAll("}", "");
            }
            return DefaultCodegen.camelize(serviceName);
        }
    }

    public class Method {
        protected String name;
        // same as service path except when squashed
        protected String path;

        Method(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }
}
