package io.limberest.config;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.limberest.service.registry.Provider;
import io.limberest.service.registry.ServiceRegistry;
import io.limberest.util.YamlLoader;

public class LimberestConfig {

    public static final String LIMBEREST_CONFIG_SYS_PROP = "io.limberest.config.file";
    public static final String LIMBEREST_CONFIG_FILE = "limberest.yaml";

    private static final Logger logger = LoggerFactory.getLogger(LimberestConfig.class);

    private Settings settings;
    public static Settings getSettings() {
        return getInstance().settings;
    }

    public class Settings extends YamlLoader {
        Settings(File file) throws IOException {
            super(file);
        }
        Settings(String string) throws IOException {
            super(string);
        }
        Settings() {
        }
    }

    /**
     * TODO: Consider service loader:
     * http://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
     * TODO: Consider env prop same as sys prop
     */
    public LimberestConfig(Provider provider) {
        try {
            String sysProp = System.getProperty(LIMBEREST_CONFIG_SYS_PROP);
            if (sysProp != null) {
                settings = new Settings(new File(sysProp));
            }
            else {
                String res = provider.getResource(LIMBEREST_CONFIG_FILE);
                if (res == null)
                    res = provider.getResource("limberest.yml"); // for cretins
                if (res == null)
                    throw new IOException("Not found: " + LIMBEREST_CONFIG_FILE);
                settings = new Settings(res);
            }
        }
        catch (IOException ex) {
            logger.warn("limberest config not found, using defaults");
            if (logger.isTraceEnabled())
                logger.trace(ex.getMessage(), ex);
            settings = new Settings();
        }
    }

    private static LimberestConfig instance;
    private static LimberestConfig getInstance() {
        if (instance == null)
            instance = new LimberestConfig(ServiceRegistry.getProvider());
        return instance;
    }
}
