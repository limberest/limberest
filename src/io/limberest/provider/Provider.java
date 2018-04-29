package io.limberest.provider;

import java.io.IOException;
import java.util.List;

import io.limberest.service.Service;
import io.limberest.service.ServiceException;

public interface Provider {

    List<String> getScanPackages() throws IOException;

    Service<?> getService(Class<? extends Service<?>> serviceClass)
            throws ServiceException, InstantiationException, IllegalAccessException;

    /**
     * returns null if not found
     */
    String loadResource(String path) throws IOException;

}
