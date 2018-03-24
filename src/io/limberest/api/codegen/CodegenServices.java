package io.limberest.api.codegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.DefaultCodegen;

/**
 * Every path equates to one service unless squashed.
 */
public class CodegenServices {

    public enum Squash {
        none,
        loose,
        tight
    }

    private Map<String,Service> services;
    private Squash squash;

    public CodegenServices(Squash squash) {
        this.squash = squash;
        this.services = new LinkedHashMap<>();
    }

    public void add(String path, CodegenOperation operation) {
        Service service = services.get(path);
        if (service == null) {
            int slashCurly = path.indexOf("/{");
            if (slashCurly == -1 || squash == Squash.none || path.substring(slashCurly + 1).indexOf("/") > 0) {
                // path becomes service name
                service = new Service(path);
                services.put(path, service);
            }
            else {
                String squashedPath = path.substring(0, slashCurly);
                Service squashedService = services.get(squashedPath);
                if (squashedService == null) {
                    service = new Service(squashedPath);
                    services.put(squashedPath, service);
                }
                else {
                    Method method = squashedService.getMethod(operation.httpMethod.toLowerCase());
                    if (squash == Squash.tight) {
                        // no new service
                        service = squashedService;
                        if (method != null) {
                            // new (longer-pathed) method will be added instead
                            squashedService.methods.remove(method);
                        }
                    }
                    else {  // LOOSE
                        // new service if method conflicts
                        if (method != null) {
                            service = new Service(path);
                            services.put(path, service);
                            // move any existing squashed methods
                            List<Method> toMove = new ArrayList<>();
                            for (Method squashedMethod : squashedService.methods) {
                                if (squashedMethod.path.equals(path))
                                    toMove.add(squashedMethod);
                            }
                            for (Method m : toMove) {
                                squashedService.methods.remove(m);
                                service.methods.add(m);
                            }
                        }
                        else {
                            service = squashedService;
                        }
                    }
                }
            }
        }

        service.methods.add(new Method(path, operation));
        // TODO: slf4j
        System.out.println("ADD: " + service);
    }

    Map<String,List<CodegenOperation>> getOperations() {
        Map<String,List<CodegenOperation>> operations = new LinkedHashMap<>();
        for (String path : services.keySet()) {
            List<CodegenOperation> ops = new ArrayList<>();
            Service service = services.get(path);
            for (Method method : service.methods) {
                CodegenOperation op = method.operation;
                if (method.path.equals(service.path)) {
                    op.path = "";
                }
                else {
                    // operation is on subpath
                    op.path = method.path.substring(service.path.length());
                }

                op.operationId = method.getName();

                op.baseName = path;
                if (op.baseName.startsWith("/"))
                    op.baseName = op.baseName.substring(1);

                ops.add(op);
            }
            operations.put(path, ops);
        }
        return operations;
    }

    Service forPath(String path) {
        String opsPath = path;
        return services.get(opsPath);
    }

    static String nameFromPath(String path) {
        return path.replace('{', '_').replaceAll("}", "");
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
        protected String path;
        protected List<Method> methods;

        Service(String path) {
            this.path = path;
            this.methods = new ArrayList<>();
        }

        Method getMethod(String name) {
            for (Method method : methods) {
                if (method.getName().equals(name))
                    return method;
            }
            return null;
        }

        /**
         * Generated class name
         */
        String getName() {
            return DefaultCodegen.camelize(path.replace('{', '_').replaceAll("}", ""));
        }

        public String toString() {
            String m = "";
            for (int i = 0; i < methods.size(); i++) {
                if (i > 0)
                    m += ",";
                m += methods.get(i).getName();
            }
            return path + " -> " + getName() + " (" + m + ")";
        }
    }

    public class Method {
        protected String path;
        protected CodegenOperation operation;

        Method(String path, CodegenOperation operation) {
            this.path = path;
            this.operation = operation;
        }

        String getName() {
            return operation.httpMethod.toLowerCase();
        }
    }
}
