package io.limberest.service.http;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import io.limberest.config.LimberestConfig;
import io.limberest.config.LimberestConfig.Settings;
import io.limberest.service.Service;
import io.limberest.service.ServiceException;
import io.limberest.service.http.Request.HttpMethod;

/**
 * TODO: Patch.
 */
public abstract class RestService<T> implements Service<T> {

    public abstract T getBody(String text) throws ServiceException;
    /**
     * @param body
     * @param prettyIndent zero means no pretty print
     * @return response text
     */
    public abstract String getText(T body, int prettyIndent) throws ServiceException;
    
    @Override
    public void initialize(Request<T> request, Principal principal, Predicate<String> roleChecker)
            throws ServiceException {
        if (principal != null)
            request.setUser(principal.getName());
        request.setUserRolePredicate(roleChecker);
    }
    
    /**
     * Return true if authentication is required.
     * Note: Containers may indicate authenticated if authentication not set up.
     */
    @Override
    public boolean isAuthenticationRequired(Request<T> request) throws ServiceException {
        return false;
    }
    
    @Override
    public boolean authorize(Request<T> request) throws ServiceException {
        List<String> roles = getRolesAllowedAccess(request);
        if (roles == null) {
            return true;
        }
        else {
            Predicate<String> roleChecker = request.getUserRolePredicate();
            if (roleChecker == null) {
                return false;
            }
            else {
                for (String role : roles) {
                    if (roleChecker.test(role))
                        return true;
                }
                return false;
            }
        }
    }
    
    /**
     * If the request user belongs to any of these roles, access is granted.
     * @return set of roles, or null if anyone's allowed
     * 
     * NOTE: {@link #isAuthenticationRequired(Request)} must return true for this 
     * to work with Java container authentication.
     */
    public List<String> getRolesAllowedAccess(Request<T> request) throws ServiceException {
        return null;
    }

    public Response<T> service(Request<T> request)
            throws ServiceException {
        if (authorize(request)) {
            String requestContent = request.getText();
            request.setBody(requestContent == null ? null : getBody(requestContent));

            Response<T> response;
            if (request.getMethod() == HttpMethod.GET) {
                response = get(request);
            }
            else if (request.getMethod() == HttpMethod.POST) {
                response =  post(request);
            }
            else if (request.getMethod() == HttpMethod.PUT) {
                response =  put(request);
            }
            else if (request.getMethod() == HttpMethod.DELETE) {
                response =  delete(request);
            }
            else if (request.getMethod() == HttpMethod.PATCH) {
                response =  patch(request);
            }
            
            else {
                throw new ServiceException(Status.NOT_IMPLEMENTED, request.getMethod() + " Not Implemented");
            }
            
            int prettyIndent = 0;
            Settings settings = LimberestConfig.getSettings();
            Map<?,?> resp = settings.getMap("response");
            if (resp != null)
                prettyIndent = settings.getInt("prettyIndent", resp);
            response.setText(getText(response.getBody(), prettyIndent));
            return response;
        }
        else {
            throw new ServiceException(Status.UNAUTHORIZED, "Not Authorized");
        }
    }

    /**
     * Retrieve an existing entity or relationship.
     */
    @GET
    public Response<T> get(Request<T> request)
            throws ServiceException {
        throw new ServiceException(Status.NOT_IMPLEMENTED, HttpMethod.GET + " not implemented");
    }

    /**
     * Create a new entity or relationship.
     */
    @POST
    public Response<T> post(Request<T> request)
            throws ServiceException {
        throw new ServiceException(Status.NOT_IMPLEMENTED, HttpMethod.POST + " not implemented");
    }

    /**
     * Update an existing entity with different values.
     */
    @PUT
    public Response<T> put(Request<T> request)
            throws ServiceException{
        throw new ServiceException(Status.NOT_IMPLEMENTED, HttpMethod.PUT + " not implemented");
    }

    /**
     * Patch an existing entity with delta values.
     */
    @PUT
    public Response<T> patch(Request<T> request)
            throws ServiceException{
        throw new ServiceException(Status.NOT_IMPLEMENTED, HttpMethod.PATCH + " not implemented");
    }
    
    /**
     * Delete an existing entity or relationship.
     * Content is supported but not recommended.
     */
    @DELETE
    public Response<T> delete(Request<T> request)
            throws ServiceException {
        throw new ServiceException(Status.NOT_IMPLEMENTED, HttpMethod.DELETE + " not implemented");
    }
}
