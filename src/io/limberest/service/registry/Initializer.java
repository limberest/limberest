package io.limberest.service.registry;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.limberest.config.LimberestConfig;
import io.limberest.config.LimberestConfig.Settings;
import io.limberest.provider.Provider;
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
    public void scan(Provider provider) throws IOException {
        List<String> scanPackages = null;
        Settings settings = LimberestConfig.getSettings();
        Map<?,?> scan = settings.getMap("scan");
        if (scan != null)
            scanPackages = settings.getStringList("packages", scan);

        if (scanPackages == null) {
            ExecutionTimer timer = new ExecutionTimer(LogLevel.Info, true);
            scanPackages = provider.getScanPackages();
            if (timer.isEnabled())
                timer.log("Limberest initializer found " + scanPackages.size() + " packages to scan in ");
            else
                logger.info("Limberest initializer found " + scanPackages.size() + " packages to scan");
            if (scanPackages.size() > 100)
                logger.info("You can restrict scanned packages through configuration (https://limberest.io/limberest/topics/config");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Limberest will scan these packages:");
            for (String scanPackage : scanPackages) {
                logger.debug("   " + scanPackage);
            }
        }

        scan(scanPackages);
    }

    public void scan(List<String> scanPackages) {
        ExecutionTimer timer = new ExecutionTimer(LogLevel.Info, true);
        for (String scanPackage : scanPackages) {
            logger.debug("Scanning for limberest services in package: {}", scanPackage);
            ConfigurationBuilder config = ConfigurationBuilder.build(scanPackage);
            config.setExpandSuperTypes(false);
            Reflections reflect = new Reflections(config);
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
            timer.log("Limberest initializer scanned " + scanPackages.size() + " packages in ");
    }
}
