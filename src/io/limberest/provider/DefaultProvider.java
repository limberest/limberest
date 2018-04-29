package io.limberest.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import io.limberest.service.Service;
import io.limberest.service.ServiceException;

public class DefaultProvider implements Provider {

    @Override
    public Service<?> getService(Class<? extends Service<?>> serviceClass)
            throws ServiceException, InstantiationException, IllegalAccessException {
        return serviceClass.newInstance();
    }

    @Override
    public String loadResource(String path) throws IOException {
        path = path.startsWith("/") ? path : "/" + path;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null)
                return null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int bytesRead;
            byte[] data = new byte[1024];
            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            buffer.flush();
            return new String(buffer.toByteArray());
        }
    }

    /**
     * Default includes packages of top-level classes of this's ClassLoader
     * but not of ancestor ClassLoaders.
     */
    @Override
    public List<String> getScanPackages() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();

        List<String> excludes = new ArrayList<>();
        ClassLoader excludeLoader = classLoader;
        while ((excludeLoader = excludeLoader.getParent()) != null) {
            for (ClassInfo classInfo : ClassPath.from(excludeLoader).getTopLevelClasses()) {
                excludes.add(classInfo.getPackageName());
            }
        }

        List<String> packageNames = new ArrayList<>();
        for (ClassInfo classInfo : ClassPath.from(classLoader).getTopLevelClasses()) {
            String packageName = classInfo.getPackageName();
            if (!packageNames.contains(packageName) && !excludes.contains(packageName)) {
                packageNames.add(packageName);
            }
        }
        return packageNames;
    }
}
