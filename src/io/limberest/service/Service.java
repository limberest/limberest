package io.limberest.service;

import java.security.Principal;
import java.util.function.Predicate;

import io.limberest.service.http.Request;
import io.limberest.service.http.Response;

public interface Service<T> {
    
    /**
     * Should access to this service require authentication?
     * @param request
     * @return true if authentication is required
     */
    public boolean isAuthenticationRequired(Request<T> request) throws ServiceException;
    
    /**
     * Determine whether access to this service is authorized for the authenticated user,
     * if any.
     * @param request
     * @return true if authorized
     */
    public boolean authorize(Request<T> request) throws ServiceException;

    /**
     * Service a request.
     * @param request
     * @return response text
     */
    public Response<T> service(Request<T> request) throws ServiceException;
    
    /**
     * Mechanism for setting up request for authorization
     * @param request
     * @param principal
     * @param roleCheck
     */
    default void initialize(Request<T> request, Principal principal, Predicate<String> roleCheck)
            throws ServiceException {
    }
}
