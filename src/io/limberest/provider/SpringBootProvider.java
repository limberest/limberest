package io.limberest.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import io.limberest.service.Service;
import io.limberest.service.ServiceException;

public class SpringBootProvider implements Provider {

    private ApplicationContext appContext;

    public SpringBootProvider(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public Service<?> getService(Class<? extends Service<?>> serviceClass)
            throws ServiceException, InstantiationException, IllegalAccessException {
        try {
            return appContext.getBean(serviceClass);

        }
        catch (NoUniqueBeanDefinitionException ex) {
            if (appContext instanceof ListableBeanFactory) {
                ListableBeanFactory beanFactory = appContext;
                Map<String,? extends Service<?>> serviceBeans = beanFactory.getBeansOfType(serviceClass);
                for (String beanName : serviceBeans.keySet()) {
                    Service<?> serviceBean = serviceBeans.get(beanName);
                    // try to resolve by matching class name exactly
                    if (serviceBean.getClass().getName().equals(serviceClass.getName()))
                        return serviceBean;
                }
            }
            throw ex;
        }
    }

    @Override
    public String loadResource(String path) throws IOException {
        Resource res = appContext.getResource("classpath:" + path);
        if (res == null)
            return null;
        try (InputStream is = res.getInputStream()) {
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

    @Override
    public List<String> getScanPackages() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        List<String> excludes = new ArrayList<>();
        ClassLoader excludeLoader = classLoader.getParent();
        if (excludeLoader != null) {
            try {
                Method method = getGetPackages(excludeLoader.getClass());
                if (method != null) {
                    method.setAccessible(true);
                    Package[] packages = (Package[]) method.invoke(excludeLoader);
                    for (Package pkg : packages) {
                        excludes.add(pkg.getName());
                    }
                }
            }
            catch (ReflectiveOperationException ex) {
                throw new IOException(ex);
            }
        }

        List<String> packages = new ArrayList<>();
        for (Package pkg : Package.getPackages()) {
            String packageName = pkg.getName();
            if (!packages.contains(packageName) && !excludes.contains(packageName)) {
                packages.add(packageName);
            }
        }

        return packages;
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

}
