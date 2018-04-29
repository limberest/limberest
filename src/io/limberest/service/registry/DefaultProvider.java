package io.limberest.service.registry;

import java.io.IOException;

import io.limberest.service.Service;
import io.limberest.service.ServiceException;
import io.limberest.util.FileLoader;

public class DefaultProvider implements Provider {

    @Override
    public Service<?> getService(Class<? extends Service<?>> serviceClass)
            throws ServiceException, InstantiationException, IllegalAccessException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getResource(String path) throws IOException {
        byte[] bytes = new FileLoader(path).readFromClassLoader();
        return bytes == null ? null : new String();
    }

}
