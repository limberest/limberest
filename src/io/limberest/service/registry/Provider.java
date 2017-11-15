package io.limberest.service.registry;

import io.limberest.service.Service;
import io.limberest.service.ServiceException;

public interface Provider {
    
    Service<?> getService(Class<? extends Service<?>> serviceClass)
            throws ServiceException, InstantiationException, IllegalAccessException;

}
