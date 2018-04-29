package io.limberest.service.registry;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.limberest.config.LimberestConfig;
import io.limberest.config.LimberestConfig.Settings;
import io.limberest.service.ResourcePath;
import io.limberest.service.http.RestService;
import io.limberest.service.registry.ServiceRegistry.RegistryKey;
import io.limberest.util.ExecutionTimer;
import io.limberest.util.ExecutionTimer.LogLevel;

/**
 * Scans for Limberest service annotations.
 */
public class Initializer {

    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);

    /**
     * By default we exclude all ancestor classloaders of the loader for this.
     */
    public void scan() throws IOException {
        List<String> scanPackages = null;
        Settings settings = LimberestConfig.getSettings();
        Map<?,?> scan = settings.getMap("scan");
        if (scan != null)
            scanPackages = settings.getStringList("packages", scan);

        if (scanPackages != null) {
            scan(scanPackages);
        }
        else {
            scan(Initializer.class.getClassLoader());
        }
    }

    public void scan(ClassLoader classLoader) throws IOException {
        ExecutionTimer timer = new ExecutionTimer(LogLevel.Info, true);
        List<String> excludes = new ArrayList<>();
        ClassLoader excludeLoader = classLoader.getParent();
        if (excludeLoader != null) {
            try {
                Method method = getGetPackages(excludeLoader.getClass());
                if (method != null) {
                    method.setAccessible(true);
                    Package[] packages = (Package[]) method.invoke(excludeLoader);
                    logger.trace("Scan excludes packages for {}:", excludeLoader);
                    for (Package pkg : packages) {
                        logger.trace("   {}", pkg.getName());
                        excludes.add(pkg.getName());
                    }
                }
            }
            catch (ReflectiveOperationException ex) {
                throw new IOException(ex);
            }
        }

        List<String> packageNames = new ArrayList<>();
        logger.trace("Scan found these packages for {}:", getClass().getClassLoader());
        for (Package pkg : Package.getPackages()) {
            String packageName = pkg.getName();
            if (!packageNames.contains(packageName) && !excludes.contains(packageName)) {
                logger.trace("   {}", pkg.getName());
                packageNames.add(packageName);
            }
        }

        if (timer.isEnabled())
            timer.log("Limberest initializer found " + packageNames.size() + " packages to scan in ");
        else
            logger.info("Limberest initializer found " + packageNames.size() + " packages to scan");
        if (packageNames.size() > 100)
            logger.info("You can restrict scanned packages through configuration (https://limberest.io/limberest/topics/config");
        scan(packageNames);
    }

    private Method getGetPackages(Class<?> pkgClass) {
        try {
            return pkgClass.getDeclaredMethod("getPackages");
        }
        catch (NoSuchMethodException ex) {
            Class<?> superClass = pkgClass.getSuperclass();
            if (superClass == null)
                return null;
            else
                return getGetPackages(superClass);
        }
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
