package io.limberest.service.registry;

import io.limberest.service.Service;
import io.limberest.service.ServiceException;

public class DefaultProvider implements Provider {

    @Override
    public Service<?> getService(Class<? extends Service<?>> serviceClass)
            throws ServiceException, InstantiationException, IllegalAccessException {
        // TODO Auto-generated method stub
        return null;
    }

}
