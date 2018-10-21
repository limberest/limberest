package io.limberest.config;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.limberest.provider.Provider;
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

        private JsonFormat json;
        public JsonFormat json() {
            if (json == null) {
                json = new JsonFormat(this);
            }
            return json;
        }
    }

    public LimberestConfig(Provider provider) {
        try {
            String sysProp = System.getProperty(LIMBEREST_CONFIG_SYS_PROP);
            if (sysProp != null) {
                settings = new Settings(new File(sysProp));
            }
            else {
                String res = provider.loadResource(LIMBEREST_CONFIG_FILE);
                if (res == null)
                    res = provider.loadResource("limberest.yml"); // for cretins
                if (res == null)
                    throw new IOException("Not found: " + LIMBEREST_CONFIG_FILE);
                settings = new Settings(res);
            }
        }
        catch (IOException ex) {
            logger.debug("limberest config not found, using defaults");
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

    @SuppressWarnings("rawtypes")
    public class JsonFormat {
        public int prettyIndent = 0;
        public boolean orderedKeys = true;
        public boolean falseValuesOutput = false;

        JsonFormat(Settings settings) {
            Map jsonMap = settings.getMap("json");
            if (jsonMap != null) {
                prettyIndent = settings.getInt("prettyIndent", jsonMap, prettyIndent);
                orderedKeys = settings.getBoolean("orderedKeys", jsonMap, orderedKeys);
                falseValuesOutput = settings.getBoolean("falseValuesOutput", jsonMap, falseValuesOutput);
            }
        }
    }
}
