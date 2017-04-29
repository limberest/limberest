package io.limberest.service.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import io.limberest.service.ResourcePath;
import io.limberest.service.http.RestService;
import io.limberest.service.registry.ServiceRegistry.RegistryKey;
import io.limberest.util.ExecutionTimer;
import io.limberest.util.ExecutionTimer.LogLevel;

/**
 * TODO optional way to set includes (packages) instead of excludes
 * TODO optional way to set classloader names to exclude
 */
public class Initializer {

    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);

    /**
     * By default we exclude all ancestor classloaders of the loader for this.
     */
    public void scan() throws IOException {
        scan(Initializer.class.getClassLoader());
    }

    public void scan(ClassLoader classLoader) throws IOException {
        ExecutionTimer timer = new ExecutionTimer(LogLevel.Info, true);
        List<String> excludes = new ArrayList<>();
        ClassLoader excludeLoader = classLoader;
        while ((excludeLoader = excludeLoader.getParent()) != null) {
            for (ClassInfo classInfo : ClassPath.from(excludeLoader).getTopLevelClasses()) {
                excludes.add(classInfo.getPackageName());
            }
        }

        List<String> packageNames = new ArrayList<>();
        for (ClassInfo classInfo : ClassPath.from(Initializer.class.getClassLoader()).getTopLevelClasses()) {
            String packageName = classInfo.getPackageName();
            if (!packageNames.contains(packageName) && !excludes.contains(packageName)) {
                packageNames.add(packageName);
            }
        }
        if (timer.isEnabled())
            timer.log("limberest initializer found " + packageNames.size() + " packages to scan in ");
        scan(packageNames);
    }

    public void scan(List<String> scanPackages) {
        ExecutionTimer timer = new ExecutionTimer(LogLevel.Info, true);
        for (String scanPackage : scanPackages) {
            logger.debug("Scanning for limberest services in package: {}", scanPackage);
            Reflections reflect = new Reflections(scanPackage);
            // service classes
            for (Class<?> classWithPath : reflect.getTypesAnnotatedWith(Path.class)) {
                logger.info("Found service class: {}", classWithPath.getName());
                Path pathAnnotation = classWithPath.getAnnotation(Path.class);
                if (pathAnnotation != null) {
                    String path = pathAnnotation.value();
                    if (path != null) {
                        logger.info("  -> and registering with path: {}", path);
                        Produces producesAnnotation = classWithPath.getAnnotation(Produces.class);
                        if (producesAnnotation != null && producesAnnotation.value() != null) {
                            // TODO can only produce one thing and consume if present must match produce
                            Consumes consumesAnnotation = classWithPath.getAnnotation(Consumes.class);
                            for (String contentType : producesAnnotation.value()) {
                                logger.info("  -> for content-type: {}", contentType);
                                @SuppressWarnings({"unchecked", "rawtypes"})
                                Class<? extends RestService<?>> serviceClass = (Class)classWithPath.asSubclass(RestService.class);
                                if (!RestService.class.isAssignableFrom(serviceClass))
                                    throw new IllegalArgumentException(serviceClass + " does not extend " + RestService.class);
                                RegistryKey key = new RegistryKey(new ResourcePath(path), contentType);
                                ServiceRegistry.getInstance().put(key, serviceClass);
                            }
                        }
                    }
                }
            }
        }
        if (timer.isEnabled())
            timer.log("limberest initializer scanned " + scanPackages.size() + " packages in ");
    }

}
