package io.limberest.api.codegen;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every path equates to one service unless squashed.
 */
public class CodegenServices {

    private boolean squash;
    private Map<String,Service> services;

    public CodegenServices() {
        this.services = new LinkedHashMap<>();
    }

    public void add(String path, String name) {
        Service service = services.get(path);
        if (service == null) {
            // check this service name on different (shorter) path
            Service existing = forName(name);
            if (existing == null) {
                service = new Service(path, name);
            }
            else {
                // need to create a unique Service for longer path
                String extra = path.substring(existing.path.length());
                service = new Service(path, name + toClassNamePart(extra));
            }
        }
        services.put(path, service);
    }

    Service forName(String name) {
        for (Service service : services.values()) {
            if (service.name.equals(name))
                return service;
        }
        return null;
    }

    Service forPath(String path) {
        for (Service service : services.values()) {
            if (service.path.equals(path))
                return service;
        }
        return null;
    }

    static String toClassNamePart(String in) {
        String out = in.replaceAll("[/{}]", "");
        out = Character.toUpperCase(out.charAt(0)) + out.substring(1);
        return out;
    }

    public class Service {
        protected String path;
        protected String name;

        Service(String path, String name) {
            this.path = path;
            this.name = name;
        }
    }
}
