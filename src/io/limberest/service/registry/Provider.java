package io.limberest.service.registry;

import java.io.IOException;

import io.limberest.service.Service;
import io.limberest.service.ServiceException;

public interface Provider {

    Service<?> getService(Class<? extends Service<?>> serviceClass)
            throws ServiceException, InstantiationException, IllegalAccessException;

    /**
     * return null if not found
     */
    String getResource(String path) throws IOException;

}
